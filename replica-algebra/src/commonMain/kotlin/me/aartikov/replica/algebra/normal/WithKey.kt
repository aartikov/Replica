package me.aartikov.replica.algebra.normal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.keyed.KeyedReplica
import me.aartikov.replica.single.Replica
import me.aartikov.replica.single.ReplicaObserver

/**
 * Converts [KeyedReplica] to [Replica] by fixing a key.
 */
fun <K : Any, T : Any> KeyedReplica<K, T>.withKey(key: K): Replica<T> {
    return WithKeyReplica(this, MutableStateFlow(key))
}

/**
 * Converts [KeyedReplica] to [Replica] by passing [StateFlow] with dynamic key.
 */
fun <K : Any, T : Any> KeyedReplica<K, T>.withKey(keyFlow: StateFlow<K?>): Replica<T> {
    return WithKeyReplica(this, keyFlow)
}

private class WithKeyReplica<K : Any, T : Any>(
    private val keyedReplica: KeyedReplica<K, T>,
    private val keyFlow: StateFlow<K?>
) : Replica<T> {

    override fun observe(
        observerCoroutineScope: CoroutineScope,
        observerActive: StateFlow<Boolean>
    ): ReplicaObserver<T> {
        return keyedReplica.observe(
            observerCoroutineScope,
            observerActive,
            keyFlow
        )
    }

    override fun refresh() {
        val key = keyFlow.value ?: return
        keyedReplica.refresh(key)
    }

    override fun revalidate() {
        val key = keyFlow.value ?: return
        keyedReplica.revalidate(key)
    }

    override suspend fun getData(forceRefresh: Boolean): T {
        val key = keyFlow.value ?: throw MissingKeyException()
        return keyedReplica.getData(key, forceRefresh)
    }
}

class MissingKeyException : Exception()