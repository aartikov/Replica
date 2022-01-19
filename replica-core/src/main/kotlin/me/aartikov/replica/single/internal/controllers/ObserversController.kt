package me.aartikov.replica.single.internal.controllers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import me.aartikov.replica.common.ObservingState
import me.aartikov.replica.common.ObservingTime
import me.aartikov.replica.single.ReplicaEvent
import me.aartikov.replica.single.ReplicaState
import me.aartikov.replica.time.TimeProvider

internal class ObserversController<T : Any>(
    private val timeProvider: TimeProvider,
    private val dispatcher: CoroutineDispatcher,
    private val replicaStateFlow: MutableStateFlow<ReplicaState<T>>,
    private val replicaEventFlow: MutableSharedFlow<ReplicaEvent<T>>
) {

    suspend fun onObserverAdded(observerUuid: String, active: Boolean) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            val observingState = state.observingState
            replicaStateFlow.value = state.copy(
                observingState = observingState.copy(
                    observerUuids = observingState.observerUuids + observerUuid,
                    activeObserverUuids = if (active) {
                        observingState.activeObserverUuids + observerUuid
                    } else {
                        observingState.activeObserverUuids
                    },
                    observingTime = if (active) {
                        ObservingTime.Now
                    } else {
                        observingState.observingTime
                    }
                )
            )

            emitObserverCountChangedEventIfRequired(
                previousObservingState = observingState,
                newObservingState = replicaStateFlow.value.observingState
            )
        }
    }

    suspend fun onObserverRemoved(observerUuid: String) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            val observingState = state.observingState

            val lastActiveObserver = observingState.activeObserverUuids.size == 1
                && observingState.activeObserverUuids.contains(observerUuid)

            replicaStateFlow.value = state.copy(
                observingState = observingState.copy(
                    observerUuids = observingState.observerUuids - observerUuid,
                    activeObserverUuids = observingState.activeObserverUuids - observerUuid,
                    observingTime = if (lastActiveObserver) {
                        ObservingTime.TimeInPast(timeProvider.currentTime)
                    } else {
                        observingState.observingTime
                    }
                )
            )

            emitObserverCountChangedEventIfRequired(
                previousObservingState = observingState,
                newObservingState = replicaStateFlow.value.observingState
            )
        }
    }

    suspend fun onObserverActive(observerUuid: String) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            val observingState = state.observingState
            replicaStateFlow.value = state.copy(
                observingState = observingState.copy(
                    activeObserverUuids = observingState.activeObserverUuids + observerUuid,
                    observingTime = ObservingTime.Now
                )
            )

            emitObserverCountChangedEventIfRequired(
                previousObservingState = observingState,
                newObservingState = replicaStateFlow.value.observingState
            )
        }
    }

    suspend fun onObserverInactive(observerUuid: String) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            val observingState = state.observingState

            val lastActiveObserver = observingState.activeObserverUuids.size == 1
                && observingState.activeObserverUuids.contains(observerUuid)

            replicaStateFlow.value = state.copy(
                observingState = observingState.copy(
                    activeObserverUuids = observingState.activeObserverUuids - observerUuid,
                    observingTime = if (lastActiveObserver) {
                        ObservingTime.TimeInPast(timeProvider.currentTime)
                    } else {
                        observingState.observingTime
                    }
                )
            )

            emitObserverCountChangedEventIfRequired(
                previousObservingState = observingState,
                newObservingState = replicaStateFlow.value.observingState
            )
        }
    }

    private suspend fun emitObserverCountChangedEventIfRequired(
        previousObservingState: ObservingState,
        newObservingState: ObservingState
    ) {
        if (previousObservingState.observerCount != newObservingState.observerCount
            || previousObservingState.activeObserverCount != newObservingState.activeObserverCount
        ) {
            replicaEventFlow.emit(
                ReplicaEvent.ObserverCountChangedEvent(
                    count = newObservingState.observerCount,
                    activeCount = newObservingState.activeObserverCount,
                    previousCount = previousObservingState.observerCount,
                    previousActiveCount = previousObservingState.activeObserverCount
                )
            )
        }
    }
}