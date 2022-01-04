package me.aartikov.replica.keyed

import me.aartikov.replica.single.OptimisticUpdate
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.RefreshCondition
import me.aartikov.replica.single.ReplicaState

interface KeyedPhysicalReplica<K : Any, T : Any> : KeyedReplica<K, T> {

    fun getCurrentState(key: K): ReplicaState<T>?

    suspend fun setData(key: K, data: T)

    suspend fun mutateData(key: K, transform: (T) -> T)

    suspend fun invalidate(
        key: K,
        refreshCondition: RefreshCondition = RefreshCondition.IfHasObservers
    )

    suspend fun makeFresh(key: K)

    fun cancelLoading(key: K)

    suspend fun clear(key: K, removeFromStorage: Boolean = true) // cancels in progress loading

    suspend fun clearError(key: K)

    suspend fun clearAll()

    suspend fun beginOptimisticUpdate(key: K, update: OptimisticUpdate<T>)

    suspend fun commitOptimisticUpdate(key: K, update: OptimisticUpdate<T>)

    suspend fun rollbackOptimisticUpdate(key: K, update: OptimisticUpdate<T>)

    suspend fun onReplica(key: K, action: suspend PhysicalReplica<T>.() -> Unit)

    suspend fun onExistingReplica(key: K, action: suspend PhysicalReplica<T>.() -> Unit)

    suspend fun onEachReplica(action: suspend PhysicalReplica<T>.(K) -> Unit)
}

suspend fun <K : Any, T : Any> KeyedPhysicalReplica<T, K>.invalidateAll(
    refreshCondition: RefreshCondition = RefreshCondition.IfHasObservers
) {
    onEachReplica {
        invalidate(refreshCondition)
    }
}