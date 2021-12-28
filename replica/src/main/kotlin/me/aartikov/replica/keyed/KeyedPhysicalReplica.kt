package me.aartikov.replica.keyed

import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaState
import me.aartikov.replica.single.invalidate

interface KeyedPhysicalReplica<K : Any, T : Any> : KeyedReplica<K, T> {

    fun getCurrentState(key: K): ReplicaState<T>?

    suspend fun setData(key: K, data: T)

    suspend fun mutateData(key: K, transform: (T) -> T)

    suspend fun makeFresh(key: K)

    suspend fun makeStale(key: K)

    fun cancelLoading(key: K)

    suspend fun clear(key: K) // cancels in progress loading

    suspend fun clearError(key: K)

    suspend fun onReplica(key: K, action: suspend PhysicalReplica<T>.() -> Unit)

    suspend fun onExistingReplica(key: K, action: suspend PhysicalReplica<T>.() -> Unit)

    suspend fun onEachReplica(action: suspend PhysicalReplica<T>.(K) -> Unit)
}

suspend fun <K : Any, T : Any> KeyedPhysicalReplica<K, T>.invalidate(key: K) {
    onExistingReplica(key) {
        invalidate()
    }
}