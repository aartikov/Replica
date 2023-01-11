package me.aartikov.replica.single

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.common.LoadingError

/**
 * Replica observer connects to a replica and receives updates: state and loading error events.
 * Replica observer can be active or inactive (this property is not exposed in the API but is used internally). Only active observer receives updates.
 * Replica observer is associated with some User Interface - a screen or a part of screen.
 */
interface ReplicaObserver<out T : Any> {

    /**
     * Flow of replica states.
     */
    val stateFlow: StateFlow<Loadable<T>>

    /**
     * Flow of loading error events.
     */
    val loadingErrorFlow: Flow<LoadingError>

    /**
     * Cancels observing manually.
     * Typically, the call of this method is not required, because an observer is tied to some coroutine scope.
     */
    fun cancelObserving()
}

/**
 * Returns current replica state.
 */
val <T : Any> ReplicaObserver<T>.currentState get() = stateFlow.value