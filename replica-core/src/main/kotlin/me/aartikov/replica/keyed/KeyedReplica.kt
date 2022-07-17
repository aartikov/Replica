package me.aartikov.replica.keyed

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.single.ReplicaObserver

interface KeyedReplica<K : Any, out T : Any> {

    fun observe(
        observerCoroutineScope: CoroutineScope,
        observerActive: StateFlow<Boolean>,
        key: StateFlow<K?>
    ): ReplicaObserver<T>

    fun refresh(key: K)

    fun revalidate(key: K)

    suspend fun getData(key: K, forceRefresh: Boolean = false): T
}