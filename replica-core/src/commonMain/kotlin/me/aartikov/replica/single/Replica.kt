package me.aartikov.replica.single

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.common.ReplicaObserverHost

/**
 * Replica is a primitive for data replication.
 * Its task is to represent a chunk of data from a server on the client side.
 * A Replica is configured using [Fetcher] and [ReplicaSettings].
 * It loads missing data when an active observer connects (see [Replica.observe]).
 * The Replica tracks data staleness.
 * It refreshes stale data when an active observer is connected.
 * The Replica deduplicates network requests (it doesn't call a new request if another one is in progress).
 * It cancels the network request when the last observer is disconnected.
 * It clears data when it has no observers for an extended period.
 *
 * The difference between [Replica] and [PhysicalReplica] is that the latter has a richer API.
 * [Replica] has a minimalistic read-only API, whereas [PhysicalReplica] allows canceling requests, modifying data, and executing optimistic updates.
 * [PhysicalReplica] extends [Replica], but not all replicas are physical replicas.
 * There are lightweight virtual replicas created by combining other replicas (see the replica-algebra module for more details).
 */
interface Replica<out T : Any> {

    /**
     * Starts observing a replica. The returned [ReplicaObserver] gives access to the replica state and error events.
     *
     * @param observerHost See [ReplicaObserverHost] for more details.
     */
    fun observe(observerHost: ReplicaObserverHost): ReplicaObserver<T>

    @Deprecated("Use observe(observerHost) instead")
    fun <T : Any> Replica<T>.observe(
        observerCoroutineScope: CoroutineScope,
        observerActive: StateFlow<Boolean>
    ): ReplicaObserver<T> = observe(ReplicaObserverHost(observerCoroutineScope, observerActive))

    /**
     * Loads fresh data from the network.
     *
     * Note: This will not trigger a new network request if another one is already in progress.
     */
    fun refresh()

    /**
     * Loads fresh data from the network if it is stale.
     *
     * Note: This will not trigger a new network request if another one is already in progress.
     */
    fun revalidate()

    /**
     * Loads and returns data, throwing an exception on error.
     * It never returns stale data and makes a network request if the data is stale.
     *
     * @param forceRefresh Forces a network request even when the data is fresh.
     *
     * Note: This will not trigger a new network request if another one is already in progress.
     */
    suspend fun getData(forceRefresh: Boolean = false): T
}