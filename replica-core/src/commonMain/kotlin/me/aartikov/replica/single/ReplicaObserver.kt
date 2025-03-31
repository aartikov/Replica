package me.aartikov.replica.single

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.common.LoadingError

/**
 * A replica observer connects to a replica and receives updates, including the replica state and loading error events.
 * A replica observer can be active or inactive (this property is used internally and is not exposed in the public API).
 * Only an active replica observer receives updates.
 * Typically, a replica observer is associated with a user interface component (e.g., a screen or a portion of a screen).
 */
interface ReplicaObserver<out T : Any> {

    /**
     * A flow of replica states.
     */
    val stateFlow: StateFlow<Loadable<T>>

    /**
     * A flow of loading error events.
     */
    val loadingErrorFlow: Flow<LoadingError>

    /**
     * Cancels observation manually.
     * Typically, calling this method is not required because the observer is tied to a coroutine scope.
     */
    fun cancelObserving()
}

/**
 * Returns the current replica state.
 */
val <T : Any> ReplicaObserver<T>.currentState get() = stateFlow.value
