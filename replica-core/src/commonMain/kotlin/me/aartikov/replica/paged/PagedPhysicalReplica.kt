package me.aartikov.replica.paged

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.common.InvalidationMode
import me.aartikov.replica.common.OptimisticUpdate
import me.aartikov.replica.common.ReplicaId
import me.aartikov.replica.common.ReplicaTag

interface PagedPhysicalReplica<I : Any, P : Page<I>> : PagedReplica<PagedData<I, P>> {

    /**
     * Unique identifier
     */
    val id: ReplicaId

    /**
     * Human readable name, used for debugging
     */
    val name: String

    /**
     * Settings, see: [PagedReplicaSettings]
     */
    val settings: PagedReplicaSettings

    /**
     * Tags that can be used for bulk operations
     */
    val tags: Set<ReplicaTag>

    /**
     * A coroutine scope that represents life time of a replica.
     */
    val coroutineScope: CoroutineScope

    /**
     * Provides [PagedReplicaState] as an observable value.
     */
    val stateFlow: StateFlow<PagedReplicaState<I, P>>

    /**
     * Notifies that some [PagedReplicaEvent] has occurred.
     */
    val eventFlow: Flow<PagedReplicaEvent<I, P>>

    /**
     * Replace current data with new [data].
     *
     * Note: It doesn't change data freshness. If previous data is missing a new data will be stale.
     */
    suspend fun setData(data: List<P>)

    /**
     * Modifies current data with [transform] function if it is exists.
     *
     * Note: It doesn't change data freshness.
     */
    suspend fun mutateData(transform: (List<P>) -> List<P>)

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
     */
    suspend fun clear(invalidationMode: InvalidationMode = InvalidationMode.DontRefresh)

    /**
     * Clears error stored in [PagedReplicaState].
     */
    suspend fun clearError()

    /**
     * Begins an optimistic update with the identifier [operationId]. The observed data will be immediately transformed by the [update].
     *
     * Note: An update with the same [operationId] will replace the previous update.
     * Note: For simple cases, it is preferable to use the [withOptimisticUpdate] extension.
     */
    suspend fun beginOptimisticUpdate(update: OptimisticUpdate<List<P>>, operationId: Any)

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
 * Returns current [PagedReplicaState].
 */
val <I : Any, P : Page<I>> PagedPhysicalReplica<I, P>.currentState get() = stateFlow.value