package me.aartikov.replica.keyed_paged

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedReplicaObserver
import me.aartikov.replica.single.ReplicaObserver

/**
 * Keyed replica replicates multiple chunks of data - different chunks for different keys.
 *
 * The difference between [KeyedReplica] and [KeyedPhysicalReplica] is that the latter has a richer API.
 * [KeyedReplica] has minimalistic read-only API, whereas [KeyedPhysicalReplica] allows to cancel requests, modify data, execute optimistic updates.
 */
interface KeyedPagedReplica<K : Any, out T : Any, out P : Page<T>> {

    /**
     * Starts to observe a keyed replica. Returned [ReplicaObserver] gives access to replica state and error events.
     * @param observerCoroutineScope represents life time of an observer. An observer will stop observing when [observerCoroutineScope] is canceled.
     * @param observerActive a [StateFlow] of observer states (active or inactive). Allows replica to know if it has active observers.
     * [key] a [StateFlow] of keys. When key is changed an observer retargets to another chunk of data.
     */
    fun observe(
        observerCoroutineScope: CoroutineScope,
        observerActive: StateFlow<Boolean>,
        key: StateFlow<K?>
    ): PagedReplicaObserver<T, P>

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