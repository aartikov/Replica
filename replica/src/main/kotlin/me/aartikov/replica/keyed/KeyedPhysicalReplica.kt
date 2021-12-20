package me.aartikov.replica.keyed

import kotlinx.coroutines.CoroutineScope
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaState
import me.aartikov.replica.single.invalidate

interface KeyedPhysicalReplica<K : Any, T : Any> : KeyedReplica<K, T> {

    val coroutineScope: CoroutineScope

    fun getState(key: K): ReplicaState<T>?

    fun setData(key: K, data: T)

    fun mutateData(key: K, transform: (T) -> T)

    fun makeFresh(key: K)

    fun makeStale(key: K)

    fun cancelLoading(key: K)

    fun clear(key: K) // cancels in progress loading

    fun clearError(key: K)

    fun onReplica(key: K, action: PhysicalReplica<T>.() -> Unit)

    fun onExistingReplica(key: K, action: PhysicalReplica<T>.() -> Unit)

    fun onEachReplica(action: PhysicalReplica<T>.(K) -> Unit)
}

fun <K : Any, T : Any> KeyedPhysicalReplica<K, T>.invalidate(key: K) {
    onExistingReplica(key) {
        invalidate()
    }
}