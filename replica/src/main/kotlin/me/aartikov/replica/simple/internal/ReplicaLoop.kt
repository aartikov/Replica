package me.aartikov.replica.simple.internal

import me.aartikov.replica.simple.ReplicaData
import me.aartikov.replica.simple.ReplicaEvent.*
import me.aartikov.replica.simple.internal.Action.*
import me.aartikov.sesame.loop.*
import me.aartikov.replica.simple.ReplicaEvent as Event
import me.aartikov.replica.simple.ReplicaState as State

internal sealed interface Action<out T : Any> {

    sealed interface LoadingAction<out T : Any> : Action<T> {
        data class Load(val dataRequested: Boolean = false) : LoadingAction<Nothing>
        object Cancel : LoadingAction<Nothing>
        data class DataLoaded<T : Any>(val data: T) : LoadingAction<T>
        object LoadingCanceled : LoadingAction<Nothing>
        data class LoadingError(val error: Exception) : LoadingAction<Nothing>
    }

    sealed interface DataChangingAction<out T : Any> : Action<T> {
        data class SetData<T : Any>(val data: T) : DataChangingAction<T>
        data class MutateData<T : Any>(val transform: (T) -> T) : DataChangingAction<T>
    }

    sealed interface FreshnessChangingAction : Action<Nothing> {
        object MakeFresh : FreshnessChangingAction
        object MakeStale : FreshnessChangingAction
    }


    sealed interface ClearAction : Action<Nothing> {
        object ClearAll : ClearAction
        object ClearError : ClearAction
    }

    sealed interface ObserverAction : Action<Nothing> {
        data class ObserverAdded(val uuid: String, val active: Boolean) : ObserverAction
        data class ObserverRemoved(val uuid: String) : ObserverAction
        data class ObserverActive(val uuid: String) : ObserverAction
        data class ObserverInactive(val uuid: String) : ObserverAction
    }
}

internal sealed class Effect<out T : Any> {
    object Load : Effect<Nothing>()
    object CancelLoading : Effect<Nothing>()
    data class EmitEvent<T : Any>(val event: Event<T>) : Effect<T>()
}

internal typealias ReplicaLoop<T> = Loop<State<T>, Action<T>, Effect<T>>

internal class ReplicaReducer<T : Any> : Reducer<State<T>, Action<T>, Effect<T>> {

    override fun reduce(
        state: State<T>,
        action: Action<T>
    ): Next<State<T>, Effect<T>> = when (action) {
        is LoadingAction -> reduceLoadingAction(state, action)
        is DataChangingAction -> reduceDataChangingAction(state, action)
        is FreshnessChangingAction -> reduceFreshnessChangingAction(state, action)
        is ClearAction -> reduceClearAction(state, action)
        is ObserverAction -> reduceObservingAction(state, action)
    }

    private fun reduceLoadingAction(
        state: State<T>,
        action: LoadingAction<T>
    ): Next<State<T>, Effect<T>> = when (action) {

        is LoadingAction.Load -> {
            when {
                state.loading -> if (action.dataRequested && !state.dataRequested) {
                    next(
                        state.copy(dataRequested = true)
                    )
                } else {
                    nothing()
                }

                else -> next(
                    state.copy(
                        loading = true,
                        dataRequested = state.dataRequested || action.dataRequested
                    ),
                    Effect.Load,
                    Effect.EmitEvent(LoadingEvent.LoadingStarted)
                )
            }
        }

        is LoadingAction.Cancel -> {
            when {
                !state.loading -> nothing()

                else -> {
                    next(
                        state.copy(
                            loading = false,
                            dataRequested = false
                        ),
                        Effect.CancelLoading
                    )
                }
            }
        }

        is LoadingAction.DataLoaded -> {
            next(
                state.copy(
                    data = ReplicaData(
                        value = action.data,
                        fresh = true
                    ),
                    error = null,
                    loading = false,
                    dataRequested = false
                ),
                Effect.EmitEvent(LoadingEvent.LoadingFinished.Success(action.data)),
                Effect.EmitEvent(FreshnessEvent.Freshened)
            )
        }

        is LoadingAction.LoadingCanceled -> {
            next(
                state.copy(
                    loading = false,
                    dataRequested = false
                ),
                Effect.EmitEvent(LoadingEvent.LoadingFinished.Canceled),
            )
        }

        is LoadingAction.LoadingError -> {
            next(
                state.copy(
                    error = action.error,
                    loading = false,
                    dataRequested = false
                ),
                Effect.EmitEvent(LoadingEvent.LoadingFinished.Error(action.error))
            )
        }
    }

    private fun reduceDataChangingAction(
        state: State<T>,
        action: DataChangingAction<T>
    ): Next<State<T>, Effect<T>> = when (action) {
        is DataChangingAction.SetData -> {
            next(
                state.copy(
                    data = if (state.data != null) {
                        state.data.copy(value = action.data)
                    } else {
                        ReplicaData(
                            value = action.data,
                            fresh = false
                        )
                    }
                ),
                Effect.EmitEvent(
                    DataChangingEvent.DataSet(action.data)
                )
            )
        }
        is DataChangingAction.MutateData -> {
            if (state.data != null) {
                val newData = state.data.copy(
                    value = action.transform(state.data.value)
                )
                next(
                    state.copy(data = newData),
                    Effect.EmitEvent(
                        DataChangingEvent.DataMutated(newData.value)
                    )
                )
            } else {
                nothing()
            }
        }
    }

    private fun reduceFreshnessChangingAction(
        state: State<T>,
        action: FreshnessChangingAction
    ): Next<State<T>, Effect<T>> = when (action) {
        is FreshnessChangingAction.MakeFresh -> {
            if (state.data != null) {
                next(
                    state.copy(
                        data = state.data.copy(fresh = true)
                    ),
                    Effect.EmitEvent(FreshnessEvent.Freshened)
                )
            } else {
                nothing()
            }
        }
        is FreshnessChangingAction.MakeStale -> {
            if (state.data?.fresh == true) {
                next(
                    state.copy(
                        data = state.data.copy(fresh = false)
                    ),
                    Effect.EmitEvent(FreshnessEvent.BecameStale)
                )
            } else {
                nothing()
            }
        }
    }

    private fun reduceClearAction(
        state: State<T>,
        action: ClearAction
    ): Next<State<T>, Effect<T>> = when (action) {
        ClearAction.ClearAll -> {
            next(
                state = state.copy(data = null),
                Effect.CancelLoading,
                Effect.EmitEvent(Event.ClearedEvent)
            )
        }
        ClearAction.ClearError -> {
            next(
                state = state.copy(error = null)
            )
        }
    }

    private fun reduceObservingAction(
        state: State<T>,
        action: ObserverAction
    ): Next<State<T>, Effect<T>> = when (action) {

        is ObserverAction.ObserverAdded -> {
            val newState = state.copy(
                observerUuids = state.observerUuids + action.uuid,
                activeObserverUuids = if (action.active) {
                    state.activeObserverUuids + action.uuid
                } else {
                    state.activeObserverUuids
                }
            )
            next(
                newState,
                emitObserverCountChangedEventIfRequired(state, newState)
            )
        }

        is ObserverAction.ObserverRemoved -> {
            val newState = state.copy(
                observerUuids = state.observerUuids - action.uuid,
                activeObserverUuids = state.activeObserverUuids - action.uuid
            )
            next(
                newState,
                emitObserverCountChangedEventIfRequired(state, newState)
            )
        }

        is ObserverAction.ObserverActive -> {
            val newState = state.copy(
                activeObserverUuids = state.activeObserverUuids + action.uuid
            )
            next(
                newState,
                emitObserverCountChangedEventIfRequired(state, newState)
            )
        }

        is ObserverAction.ObserverInactive -> {
            val newState = state.copy(
                activeObserverUuids = state.activeObserverUuids - action.uuid
            )
            next(
                newState,
                emitObserverCountChangedEventIfRequired(state, newState)
            )
        }
    }

    private fun emitObserverCountChangedEventIfRequired(
        previousState: State<T>,
        newState: State<T>
    ): Effect.EmitEvent<T>? {
        return if (previousState.observerCount != newState.observerCount
            || previousState.activeObserverCount != newState.activeObserverCount
        ) {
            Effect.EmitEvent(
                Event.ObserverCountChanged(
                    count = newState.observerCount,
                    activeCount = newState.activeObserverCount,
                    previousCount = previousState.observerCount,
                    previousActiveCount = previousState.activeObserverCount
                )
            )
        } else {
            null
        }
    }
}