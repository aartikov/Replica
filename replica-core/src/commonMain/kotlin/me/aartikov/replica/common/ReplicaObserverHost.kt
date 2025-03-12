package me.aartikov.replica.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * ReplicaObserverHost is an entity that enables the creation of observers for replicas.
 * It manages the observer's lifecycle and controls its active state.
 *
 * [observerCoroutineScope] represents the lifetime of an observer. The observer will stop observing when [observerCoroutineScope] is canceled.
 * [observerActive] is a [StateFlow] indicating whether the observer is active. This allows the replica to determine if it has active observers.
 */
interface ReplicaObserverHost {

    val observerCoroutineScope: CoroutineScope

    val observerActive: StateFlow<Boolean>
}

private class StandardReplicaObserverHost(
    override val observerCoroutineScope: CoroutineScope,
    override val observerActive: StateFlow<Boolean>
) : ReplicaObserverHost

fun ReplicaObserverHost(
    observerCoroutineScope: CoroutineScope,
    observerActive: StateFlow<Boolean>
): ReplicaObserverHost = StandardReplicaObserverHost(observerCoroutineScope, observerActive)