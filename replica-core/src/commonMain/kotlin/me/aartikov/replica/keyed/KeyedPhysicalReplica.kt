package me.aartikov.replica.keyed

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.common.InvalidationMode
import me.aartikov.replica.common.OptimisticUpdate
import me.aartikov.replica.common.ReplicaId
import me.aartikov.replica.common.ReplicaTag
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaState

/**
 * Keyed replica replicates multiple chunks of data - different chunks for different keys.
 * Internally it uses [PhysicalReplica] for individual data chunks.
 *
 * The difference between [KeyedReplica] and [KeyedPhysicalReplica] is that the latter has a richer API.
 * [KeyedReplica] has minimalistic read-only API, whereas [KeyedPhysicalReplica] allows to cancel requests, modify data, execute optimistic updates.
 */
interface KeyedPhysicalReplica<K : Any, T : Any> : KeyedReplica<K, T> {

    /**
     * Unique identifier
     */
    val id: ReplicaId

    /**
     * Human readable name, used for debugging
     */
    val name: String

    /**
     * Settings, see: [KeyedReplicaSettings]
     */
    val settings: KeyedReplicaSettings<K, T>

    /**
     * Tags that can be used for bulk operations
     */
    val tags: Set<ReplicaTag>

    /**
     * A coroutine scope that represents life time of a keyed replica.
     */
    val coroutineScope: CoroutineScope

    /**
     * Provides [KeyedReplicaState] as an observable value.
     */
    val stateFlow: StateFlow<KeyedReplicaState>

    /**
     * Notifies that some [KeyedReplicaEvent] has occurred.
     */
    val eventFlow: Flow<KeyedReplicaEvent<K, T>>

    /**
     * Returns current [ReplicaState] for a given [key].
     */
    fun getCurrentState(key: K): ReplicaState<T>?

    /**
     * Replace current data with new [data] for a given [key].
     *
     * Note: It doesn't change data freshness. If previous data is missing a new data will be stale.
     */
    suspend fun setData(key: K, data: T)

    /**
     * Modifies current data with [transform] function if it is exists for a given [key].
     *
     * Note: It doesn't change data freshness.
     */
    suspend fun mutateData(key: K, transform: (T) -> T)

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
     * @param removeFromStorage specifies if data will be removed from [KeyedStorage].
     */
    suspend fun clear(key: K, removeFromStorage: Boolean = true)

    /**
     * Clears error stored in [ReplicaState] for a given [key].
     */
    suspend fun clearError(key: K)

    /**
     * Cancels network requests and clears data in all child replicas.
     */
    suspend fun clearAll()

    /**
     * Begins optimistic update for a given [key]. Observed data will be transformed by [update] function immediately.
     *
     * Note: for simple cases it is better to use [withOptimisticUpdate] extension.
     */
    suspend fun beginOptimisticUpdate(key: K, update: OptimisticUpdate<T>)

    /**
     * Commits optimistic update for a given [key]. Child replica forgets previous data.
     *
     * Note: for simple cases it is better to use [withOptimisticUpdate] extension.
     */
    suspend fun commitOptimisticUpdate(key: K, update: OptimisticUpdate<T>)

    /**
     * Rollbacks optimistic update for a given [key]. Observed data will be replaced to the originaal one.
     *
     * Note: for simple cases it is better to use [withOptimisticUpdate] extension.
     */
    suspend fun rollbackOptimisticUpdate(key: K, update: OptimisticUpdate<T>)

    /**
     * Executes an [action] on a [PhysicalReplica] with a given [key]. If the replica doesn't exist it is created.
     */
    suspend fun onReplica(key: K, action: suspend PhysicalReplica<T>.() -> Unit)

    /**
     * Executes an [action] on a [PhysicalReplica] with a given [key]. If the replica doesn't exist the action is not executed.
     */
    suspend fun onExistingReplica(key: K, action: suspend PhysicalReplica<T>.() -> Unit)

    /**
     * Executes an [action] on each child [PhysicalReplica].
     */
    suspend fun onEachReplica(action: suspend PhysicalReplica<T>.(K) -> Unit)
}

/**
 * Returns current [KeyedReplicaState].
 */
val <K : Any, T : Any> KeyedPhysicalReplica<K, T>.currentState get() = stateFlow.value