package me.aartikov.replica.single.internal.controllers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import me.aartikov.replica.single.ReplicaEvent
import me.aartikov.replica.single.ReplicaState

internal class ObserversController<T : Any>(
    private val dispatcher: CoroutineDispatcher,
    private val replicaStateFlow: MutableStateFlow<ReplicaState<T>>,
    private val replicaEventFlow: MutableSharedFlow<ReplicaEvent<T>>
) {

    suspend fun onObserverAdded(observerUuid: String, active: Boolean) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            replicaStateFlow.value = state.copy(
                observerUuids = state.observerUuids + observerUuid,
                activeObserverUuids = if (active) {
                    state.activeObserverUuids + observerUuid
                } else {
                    state.activeObserverUuids
                }
            )

            emitObserverCountChangedEventIfRequired(
                previousState = state,
                newState = replicaStateFlow.value
            )
        }
    }

    suspend fun onObserverRemoved(observerUuid: String) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            replicaStateFlow.value = state.copy(
                observerUuids = state.observerUuids - observerUuid,
                activeObserverUuids = state.activeObserverUuids - observerUuid
            )

            emitObserverCountChangedEventIfRequired(
                previousState = state,
                newState = replicaStateFlow.value
            )
        }
    }

    suspend fun onObserverActive(observerUuid: String) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            replicaStateFlow.value = state.copy(
                activeObserverUuids = state.activeObserverUuids + observerUuid
            )

            emitObserverCountChangedEventIfRequired(
                previousState = state,
                newState = replicaStateFlow.value
            )
        }
    }

    suspend fun onObserverInactive(observerUuid: String) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            replicaStateFlow.value = state.copy(
                activeObserverUuids = state.activeObserverUuids - observerUuid
            )

            emitObserverCountChangedEventIfRequired(
                previousState = state,
                newState = replicaStateFlow.value
            )
        }
    }

    private suspend fun emitObserverCountChangedEventIfRequired(
        previousState: ReplicaState<T>,
        newState: ReplicaState<T>
    ) {
        if (previousState.observerCount != newState.observerCount
            || previousState.activeObserverCount != newState.activeObserverCount
        ) {
            replicaEventFlow.emit(
                ReplicaEvent.ObserverCountChanged(
                    count = newState.observerCount,
                    activeCount = newState.activeObserverCount,
                    previousCount = previousState.observerCount,
                    previousActiveCount = previousState.activeObserverCount
                )
            )
        }
    }
}