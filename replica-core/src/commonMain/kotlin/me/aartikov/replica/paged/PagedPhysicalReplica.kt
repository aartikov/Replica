package me.aartikov.replica.paged

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.common.InvalidationMode
import me.aartikov.replica.common.OptimisticUpdate
import me.aartikov.replica.common.ReplicaId
import me.aartikov.replica.common.ReplicaTag

interface PagedPhysicalReplica<T : Any, P : Page<T>> : PagedReplica<T, P> {

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
    val stateFlow: StateFlow<PagedReplicaState<T, P>>

    /**
     * Notifies that some [ReplicaEvent] has occurred.
     */
    val eventFlow: Flow<PagedReplicaEvent<T, P>>

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
     */
    suspend fun clear()

    /**
     * Clears error stored in [PagedReplicaState].
     */
    suspend fun clearError()

    /**
     * Begins optimistic update. Observed data will be transformed by [update] function immediately.
     *
     * Note: for simple cases it is better to use [withOptimisticUpdate] helper function.
     */
    suspend fun beginOptimisticUpdate(update: OptimisticUpdate<List<P>>)

    /**
     * Commits optimistic update. Replica forgets previous data.
     *
     * Note: for simple cases it is better to use [withOptimisticUpdate] helper function.
     */
    suspend fun commitOptimisticUpdate(update: OptimisticUpdate<List<P>>)

    /**
     * Rollbacks optimistic update. Observed data will be replaced to the previous one.
     *
     * Note: for simple cases it is better to use [withOptimisticUpdate] helper function.
     */
    suspend fun rollbackOptimisticUpdate(update: OptimisticUpdate<List<P>>)
}

/**
 * Returns current [PagedReplicaState].
 */
val <T : Any, P : Page<T>> PagedPhysicalReplica<T, P>.currentState get() = stateFlow.value