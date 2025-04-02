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
 * Its task is to represent a chunk of data from a server on the client side.
 * A Replica is configured using [Fetcher] and [ReplicaSettings].
 * It loads missing data when an active observer connects (see [Replica.observe]).
 * It tracks data staleness.
 * It refreshes stale data when an active observer is connected.
 * It deduplicates network requests (it doesn't call a new request if another one is in progress).
 * It cancels the network request when the last observer is disconnected.
 * It clears data when it has no observers for an extended period.
 *
 * The difference between [Replica] and [PhysicalReplica] is that the latter has a richer API.
 * [Replica] provides a minimalistic read-only API, whereas [PhysicalReplica] allows canceling requests, modifying data, and executing optimistic updates.
 * [PhysicalReplica] extends [Replica], but not all replicas are physical replicas.
 * There are lightweight virtual replicas created by combining other replicas (see the replica-algebra module for more details).
 */
interface PhysicalReplica<T : Any> : Replica<T> {

    /**
     * Unique identifier.
     */
    val id: ReplicaId

    /**
     * Human-readable name used for debugging.
     */
    val name: String

    /**
     * Settings. See [ReplicaSettings].
     */
    val settings: ReplicaSettings

    /**
     * Tags that can be used for bulk operations.
     */
    val tags: Set<ReplicaTag>

    /**
     * A CoroutineScope representing the lifetime of this replica.
     */
    val coroutineScope: CoroutineScope

    /**
     * Provides the [ReplicaState] as an observable value.
     */
    val stateFlow: StateFlow<ReplicaState<T>>

    /**
     * Emits [ReplicaEvent] notifications.
     */
    val eventFlow: Flow<ReplicaEvent<T>>

    /**
     * Replaces the current data with new [data].
     *
     * Note: This does not change data freshness. If the previous data is missing, the new data will be considered stale.
     */
    suspend fun setData(data: T)

    /**
     * Modifies the current data using the [transform] function if it exists.
     *
     * Note: This does not change data freshness.
     */
    suspend fun mutateData(transform: (T) -> T)

    /**
     * Makes the data stale if it exists. It may also trigger a refresh depending on [InvalidationMode].
     */
    suspend fun invalidate(mode: InvalidationMode = InvalidationMode.RefreshIfHasObservers)

    /**
     * Makes the data fresh if it exists.
     */
    suspend fun makeFresh()

    /**
     * Cancels the current request if it is in progress.
     */
    fun cancel()

    /**
     * Cancels the current request and clears the data.
     *
     * @param invalidationMode Specifies how the replica refreshes data. See [InvalidationMode].
     * @param removeFromStorage Specifies whether the data will be removed from [Storage].
     */
    suspend fun clear(
        invalidationMode: InvalidationMode = InvalidationMode.DontRefresh,
        removeFromStorage: Boolean = true
    )

    /**
     * Clears the error stored in [ReplicaState].
     */
    suspend fun clearError()

    /**
     * Begins an optimistic update with the identifier [operationId]. The observed data will be immediately transformed by the [update].
     *
     * Note: An update with the same [operationId] will replace the previous update.
     * Note: For simple cases, it is preferable to use the [withOptimisticUpdate] extension.
     */
    suspend fun beginOptimisticUpdate(update: OptimisticUpdate<T>, operationId: Any)

    /**
     * Commits the optimistic update with a given [operationId]. Replica forgets the original data.
     *
     * Note: For simple cases, it is preferable to use the [withOptimisticUpdate] extension.
     */
    suspend fun commitOptimisticUpdate(operationId: Any)

    /**
     * Rolls back the optimistic update with a given [operationId]. The observed data will be reverted to the original state.
     *
     * Note: For simple cases, it is preferable to use the [withOptimisticUpdate] extension.
     */
    suspend fun rollbackOptimisticUpdate(operationId: Any)
}

/**
 * Returns the current [ReplicaState].
 */
val <T : Any> PhysicalReplica<T>.currentState get() = stateFlow.value
