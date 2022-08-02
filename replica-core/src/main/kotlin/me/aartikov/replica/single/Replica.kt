package me.aartikov.replica.single

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * Replica is a primitive for data replication.
 * The replica's task is to represent some chunk of data from a server on a client side.
 * Replica is configured by [Fetcher] and [ReplicaSettings].
 * Replica loads missing data when an active observer connects (see: [Replica.observe]).
 * Replica keeps track of data staleness.
 * Replica refreshes stale data when an active observer is connected.
 * Replica deduplicates network requests (it doesn't coll a new request if another one is in progress).
 * Replica cancels network request when a last observer is disconnected.
 * Replica clears data when it has no observers for a long time.
 *
 * The difference between [Replica] and [PhysicalReplica] is that the latter has a richer API.
 * [Replica] has minimalistic read-only API, whereas [PhysicalReplica] allows to cancel requests, modify data, execute optimistic updates.
 * [PhysicalReplica] extends [Replica], but not all replicas are physical replicas.
 * There are lightweight virtual replicas created by combining other replicas (see: replica-algebra module for more details).
 */
interface Replica<out T : Any> {

    /**
     * Starts to observe a replica. Returned [ReplicaObserver] gives access to replica state and error events.
     * @param observerCoroutineScope represents life time of an observer. An observer will stop observing when [observerCoroutineScope] is canceled.
     * @param observerActive a [StateFlow] of observer states (active or inactive). Allows replica to know if it has active observers.
     */
    fun observe(
        observerCoroutineScope: CoroutineScope,
        observerActive: StateFlow<Boolean>
    ): ReplicaObserver<T>

    /**
     * Loads fresh data from a network.
     *
     * Note: it will not lead to a new network request if another one is in progress.
     */
    fun refresh()

    /**
     * Loads fresh data from a network if it is stale.
     *
     * Note: it will not lead to a new network request if another one is in progress.
     */
    fun revalidate()

    /**
     * Loads and returns data. Throws an exception on error.
     * It never returns stale data. It makes a network request if data is stale.
     *
     * @param forceRefresh forces to make a network request even when data is fresh.
     *
     * Note: it will not lead to a new network request if another one is in progress.
     */
    suspend fun getData(forceRefresh: Boolean = false): T
}