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

    suspend fun onObserverAdded(observerId: Long, active: Boolean) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            val observingState = state.observingState
            replicaStateFlow.value = state.copy(
                observingState = observingState.copy(
                    observerIds = observingState.observerIds + observerId,
                    activeObserverIds = if (active) {
                        observingState.activeObserverIds + observerId
                    } else {
                        observingState.activeObserverIds
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

    suspend fun onObserverRemoved(observerId: Long) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            val observingState = state.observingState

            val lastActiveObserver = observingState.activeObserverIds.size == 1
                    && observingState.activeObserverIds.contains(observerId)

            replicaStateFlow.value = state.copy(
                observingState = observingState.copy(
                    observerIds = observingState.observerIds - observerId,
                    activeObserverIds = observingState.activeObserverIds - observerId,
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

    suspend fun onObserverActive(observerId: Long) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            val observingState = state.observingState
            replicaStateFlow.value = state.copy(
                observingState = observingState.copy(
                    activeObserverIds = observingState.activeObserverIds + observerId,
                    observingTime = ObservingTime.Now
                )
            )

            emitObserverCountChangedEventIfRequired(
                previousObservingState = observingState,
                newObservingState = replicaStateFlow.value.observingState
            )
        }
    }

    suspend fun onObserverInactive(observerId: Long) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            val observingState = state.observingState

            val lastActiveObserver = observingState.activeObserverIds.size == 1
                    && observingState.activeObserverIds.contains(observerId)

            replicaStateFlow.value = state.copy(
                observingState = observingState.copy(
                    activeObserverIds = observingState.activeObserverIds - observerId,
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