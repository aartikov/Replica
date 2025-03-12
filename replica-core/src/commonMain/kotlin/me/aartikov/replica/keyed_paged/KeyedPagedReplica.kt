package me.aartikov.replica.keyed_paged

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.common.ReplicaObserverHost
import me.aartikov.replica.paged.PagedReplicaObserver
import me.aartikov.replica.single.ReplicaObserver

/**
 * Keyed replica replicates multiple chunks of data - different chunks for different keys.
 *
 * The difference between [KeyedPagedReplica] and [KeyedPagedPhysicalReplica] is that the latter has a richer API.
 * [KeyedPagedReplica] has minimalistic read-only API, whereas [KeyedPagedPhysicalReplica] allows to cancel requests, modify data, execute optimistic updates.
 */
interface KeyedPagedReplica<K : Any, out T : Any> {

    /**
     * Starts to observe a keyed replica. Returned [ReplicaObserver] gives access to replica state and error events.
     * @param observerHost - see: [ReplicaObserverHost]
     * @param keyFlow] - a [StateFlow] of keys. When key is changed an observer retargets to another chunk of data.
     */
    fun observe(observerHost: ReplicaObserverHost, keyFlow: StateFlow<K?>): PagedReplicaObserver<T>

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

    fun loadNext(key: K)

    fun loadPrevious(key: K)

}

@Deprecated("Use observe(observerHost) instead")
fun <K : Any, T : Any> KeyedPagedReplica<K, T>.observe(
    observerCoroutineScope: CoroutineScope,
    observerActive: StateFlow<Boolean>,
    keyFlow: StateFlow<K?>
): PagedReplicaObserver<T> {
    return observe(ReplicaObserverHost(observerCoroutineScope, observerActive), keyFlow)
}
