package me.aartikov.replica.single

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * Replica is a primitive for data replication.
 * The replica's task is to represent some chunk of data from a server on a client side.
 * Replica is configured by [Fetcher] and [ReplicaSettings].
 * Replica replicates data only when there is some active observer (see: [Replica.observe]).
 * Replica keeps track of data staleness and refreshes it automatically.
 * Replica can cancel requests automatically when user leaves a screen.
 * Replica can clear data automatically when it is not required anymore.
 *
 * The difference between [Replica] and [PhysicalReplica] is that the latter has a richer API.
 * [Replica] has minimalistic read-only API, whereas [PhysicalReplica] allows to cancel requests, modify data, execute optimistic updates.
 * [PhysicalReplica] extends [Replica], but not all replicas are physical replicas.
 * There are lightweight virtual replicas created by combining other replicas (see: replica-algebra module for more details).
 */
interface Replica<out T : Any> {

    fun observe(
        observerCoroutineScope: CoroutineScope,
        observerActive: StateFlow<Boolean>
    ): ReplicaObserver<T>

    fun refresh()

    fun revalidate()

    suspend fun getData(forceRefresh: Boolean = false): T
}