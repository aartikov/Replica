package me.aartikov.replica.algebra

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.keyed.KeyedReplica
import me.aartikov.replica.single.Replica
import me.aartikov.replica.single.ReplicaObserver

internal class WithKeyReplica<K : Any, T : Any>(
    private val keyedReplica: KeyedReplica<K, T>,
    private val key: K
) : Replica<T> {

    override fun observe(
        observerCoroutineScope: CoroutineScope,
        observerActive: StateFlow<Boolean>
    ): ReplicaObserver<T> {
        return keyedReplica.observe(
            observerCoroutineScope,
            observerActive,
            MutableStateFlow(key)
        )
    }

    override fun refresh() {
        keyedReplica.refresh(key)
    }

    override fun revalidate() {
        keyedReplica.revalidate(key)
    }

    override suspend fun getData(): T {
        return keyedReplica.getData(key)
    }

    override suspend fun getRefreshedData(): T {
        return keyedReplica.getRefreshedData(key)
    }
}

fun <K : Any, T : Any> KeyedReplica<K, T>.withKey(key: K): Replica<T> {
    return WithKeyReplica(this, key)
}