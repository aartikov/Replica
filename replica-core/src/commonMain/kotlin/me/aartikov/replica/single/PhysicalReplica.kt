package me.aartikov.replica.single

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.common.InvalidationMode
import me.aartikov.replica.common.OptimisticUpdate
import me.aartikov.replica.common.ReplicaId
import me.aartikov.replica.common.ReplicaTag

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
interface PhysicalReplica<T : Any> : Replica<T> {

    /**
     * Unique identifier
     */
    val id: ReplicaId

    /**
     * Human readable name, used for debugging
     */
    val name: String

    /**
     * Settings, see: [ReplicaSettings]
     */
    val settings: ReplicaSettings

    /**
     * Tags that can be used for bulk operations
     */
    val tags: Set<ReplicaTag>

    /**
     * A coroutine scope that represents life time of a replica.
     */
    val coroutineScope: CoroutineScope

    /**
     * Provides [ReplicaState] as an observable value.
     */
    val stateFlow: StateFlow<ReplicaState<T>>

    /**
     * Notifies that some [ReplicaEvent] has occurred.
     */
    val eventFlow: Flow<ReplicaEvent<T>>

    /**
     * Replace current data with new [data].
     *
     * Note: It doesn't change data freshness. If previous data is missing a new data will be stale.
     */
    suspend fun setData(data: T)

    /**
     * Modifies current data with [transform] function if it is exists.
     *
     * Note: It doesn't change data freshness.
     */
    suspend fun mutateData(transform: (T) -> T)

    /**
     * Makes data stale if it is exists. It also could call a refresh depending on [InvalidationMode].
     */
    suspend fun invalidate(mode: InvalidationMode = InvalidationMode.RefreshIfHasObservers)

    /**
     * Makes data fresh if it is exists.
     */
    suspend fun makeFresh()

    /**
     * Cancels current request if it is in progress.
     */
    fun cancel()

    /**
     * Cancels current request and clears data.
     *
     * @param invalidationMode specifies how a replica refreshes data. See: [InvalidationMode].
     * @param removeFromStorage specifies if data will be removed from [Storage].
     */
    suspend fun clear(
        invalidationMode: InvalidationMode = InvalidationMode.DontRefresh,
        removeFromStorage: Boolean = true
    )

    /**
     * Clears error stored in [ReplicaState].
     */
    suspend fun clearError()

    /**
     * Begins optimistic update. Observed data will be transformed by [update] function immediately.
     *
     * Note: for simple cases it is better to use [withOptimisticUpdate] extension.
     */
    suspend fun beginOptimisticUpdate(update: OptimisticUpdate<T>)

    /**
     * Commits optimistic update. Replica forgets previous data.
     *
     * Note: for simple cases it is better to use [withOptimisticUpdate] extension.
     */
    suspend fun commitOptimisticUpdate(update: OptimisticUpdate<T>)

    /**
     * Rollbacks optimistic update. Observed data will be replaced to the original one.
     *
     * Note: for simple cases it is better to use [withOptimisticUpdate] extension.
     */
    suspend fun rollbackOptimisticUpdate(update: OptimisticUpdate<T>)
}

/**
 * Returns current [ReplicaState].
 */
val <T : Any> PhysicalReplica<T>.currentState get() = stateFlow.value