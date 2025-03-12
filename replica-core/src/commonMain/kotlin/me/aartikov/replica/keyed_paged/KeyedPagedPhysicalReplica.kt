package me.aartikov.replica.keyed_paged

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.common.InvalidationMode
import me.aartikov.replica.common.OptimisticUpdate
import me.aartikov.replica.common.ReplicaId
import me.aartikov.replica.common.ReplicaTag
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedData
import me.aartikov.replica.paged.PagedPhysicalReplica
import me.aartikov.replica.paged.PagedReplicaState
import me.aartikov.replica.single.ReplicaState

/**
 * Keyed replica replicates multiple chunks of data - different chunks for different keys.
 * Internally it uses [PagedPhysicalReplica] for individual data chunks.
 *
 * The difference between [KeyedPagedReplica] and [KeyedPagedPhysicalReplica] is that the latter
 * has a richer API.
 * [KeyedPagedReplica] has minimalistic read-only API, whereas [KeyedPagedPhysicalReplica] allows to cancel requests, modify data, execute optimistic updates.
 */
interface KeyedPagedPhysicalReplica<K : Any, I : Any, P : Page<I>> :
    KeyedPagedReplica<K, PagedData<I, P>> {

    /**
     * Unique identifier
     */
    val id: ReplicaId

    /**
     * Human readable name, used for debugging
     */
    val name: String

    /**
     * Settings, see: [KeyedPagedReplicaSettings]
     */
    val settings: KeyedPagedReplicaSettings<K, I, P>

    /**
     * Tags that can be used for bulk operations
     */
    val tags: Set<ReplicaTag>

    /**
     * A coroutine scope that represents life time of a keyed replica.
     */
    val coroutineScope: CoroutineScope

    /**
     * Provides [KeyedPagedReplicaState] as an observable value.
     */
    val stateFlow: StateFlow<KeyedPagedReplicaState>

    /**
     * Notifies that some [KeyedPagedReplicaEvent] has occurred.
     */
    val eventFlow: Flow<KeyedPagedReplicaEvent<K, I, P>>

    /**
     * Returns current [ReplicaState] for a given [key].
     */
    fun getCurrentState(key: K): PagedReplicaState<I, P>?

    /**
     * Replace current data with new [data] for a given [key].
     *
     * Note: It doesn't change data freshness. If previous data is missing a new data will be stale.
     */
    suspend fun setData(key: K, data: List<P>)

    /**
     * Modifies current data with [transform] function if it is exists for a given [key].
     *
     * Note: It doesn't change data freshness.
     */
    suspend fun mutateData(key: K, transform: (List<P>) -> List<P>)

    /**
     * Makes data stale if it is exists for a given [key]. It also could call a refresh depending on [InvalidationMode].
     */
    suspend fun invalidate(key: K, mode: InvalidationMode = InvalidationMode.RefreshIfHasObservers)

    /**
     * Makes data fresh if it is exists for a given [key].
     */
    suspend fun makeFresh(key: K)

    /**
     * Cancels current request if it is in progress for a given [key].
     */
    fun cancel(key: K)

    /**
     * Cancels current request and clears data for a given [key].
     *
     * @param invalidationMode specifies how a replica refreshes data. See: [InvalidationMode].
     */
    suspend fun clear(
        key: K,
        invalidationMode: InvalidationMode = InvalidationMode.DontRefresh
    )

    /**
     * Clears error stored in [PagedReplicaState] for a given [key].
     */
    suspend fun clearError(key: K)

    /**
     * Cancels network requests and clears data in all child replicas.
     *
     * @param invalidationMode specifies how a replica refreshes data. See: [InvalidationMode].
     */
    suspend fun clearAll(invalidationMode: InvalidationMode = InvalidationMode.DontRefresh)

    /**
     * Begins optimistic update for a given [key]. Observed data will be transformed by [update] function immediately.
     *
     * Note: for simple cases it is better to use [withOptimisticUpdate] extension.
     */
    suspend fun beginOptimisticUpdate(key: K, update: OptimisticUpdate<List<P>>)

    /**
     * Commits optimistic update for a given [key]. Child replica forgets previous data.
     *
     * Note: for simple cases it is better to use [withOptimisticUpdate] extension.
     */
    suspend fun commitOptimisticUpdate(key: K, update: OptimisticUpdate<List<P>>)

    /**
     * Rollbacks optimistic update for a given [key]. Observed data will be replaced to the original one.
     *
     * Note: for simple cases it is better to use [withOptimisticUpdate] extension.
     */
    suspend fun rollbackOptimisticUpdate(key: K, update: OptimisticUpdate<List<P>>)

    /**
     * Executes an [action] on a [PagedPhysicalReplica] with a given [key]. If the replica doesn't exist it is created.
     */
    suspend fun onPagedReplica(key: K, action: suspend PagedPhysicalReplica<I, P>.() -> Unit)

    /**
     * Executes an [action] on a [PagedPhysicalReplica] with a given [key]. If the replica doesn't exist the action is not executed.
     */
    suspend fun onExistingPagedReplica(key: K, action: suspend PagedPhysicalReplica<I, P>.() -> Unit)

    /**
     * Executes an [action] on each child [PagedPhysicalReplica].
     */
    suspend fun onEachPagedReplica(action: suspend PagedPhysicalReplica<I, P>.(K) -> Unit)
}

/**
 * Returns current [KeyedPagedReplicaState].
 */
val <K : Any, I : Any, P : Page<I>> KeyedPagedPhysicalReplica<K, I, P>.currentState get() = stateFlow.value