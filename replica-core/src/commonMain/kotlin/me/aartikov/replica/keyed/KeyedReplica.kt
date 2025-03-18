package me.aartikov.replica.keyed

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.common.ReplicaObserverHost
import me.aartikov.replica.single.ReplicaObserver

/**
 * Keyed replica replicates multiple chunks of data - different chunks for different keys.
 *
 * The difference between [KeyedReplica] and [KeyedPhysicalReplica] is that the latter has a richer API.
 * [KeyedReplica] has minimalistic read-only API, whereas [KeyedPhysicalReplica] allows to cancel requests, modify data, execute optimistic updates.
 */
interface KeyedReplica<K : Any, out T : Any> {

    /**
     * Starts to observe a keyed replica. Returned [ReplicaObserver] gives access to replica state and error events.
     * @param observerHost - see: [ReplicaObserverHost]
     * @param keyFlow - a [StateFlow] of keys. When key is changed an observer retargets to another chunk of data.
     */
    fun observe(observerHost: ReplicaObserverHost, keyFlow: StateFlow<K?>): ReplicaObserver<T>

    /**
     * Loads fresh data from a network for a given [key].
     *
     * Note: it will not lead to a new network request if another one with the same key is in progress.
     */
    fun refresh(key: K)

    /**
     * Loads fresh data from a network for a given [key] if it is stale.
     *
     * Note: it will not lead a to new network request if another one with the same key is in progress.
     */
    fun revalidate(key: K)

    /**
     * Loads and returns data for a given [key]. Throws an exception on error.
     * It never returns stale data. It makes a network request if data is stale.
     *
     * Note: it will not lead to a new network request if another one with the same key is in progress.
     *
     * @param forceRefresh forces to make a network request even when data is fresh.
     *
     */
    suspend fun getData(key: K, forceRefresh: Boolean = false): T
}

@Deprecated("Use observe(observerHost) instead")
fun <K : Any, T : Any> KeyedReplica<K, T>.observe(
    observerCoroutineScope: CoroutineScope,
    observerActive: StateFlow<Boolean>,
    keyFlow: StateFlow<K?>
): ReplicaObserver<T> {
    return observe(ReplicaObserverHost(observerCoroutineScope, observerActive), keyFlow)
}
