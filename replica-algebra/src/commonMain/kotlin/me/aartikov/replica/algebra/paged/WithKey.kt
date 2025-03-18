package me.aartikov.replica.algebra.paged

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.common.ReplicaObserverHost
import me.aartikov.replica.keyed_paged.KeyedPagedReplica
import me.aartikov.replica.paged.PagedReplica
import me.aartikov.replica.paged.PagedReplicaObserver

/**
 * Converts [KeyedPagedReplica] to [PagedReplica] by fixing a key.
 */
fun <K : Any, T : Any> KeyedPagedReplica<K, T>.withKey(key: K): PagedReplica<T> {
    return WithKeyReplica(this, MutableStateFlow(key))
}

/**
 * Converts [KeyedPagedReplica] to [PagedReplica] by passing [StateFlow] with dynamic key.
 */
fun <K : Any, T : Any> KeyedPagedReplica<K, T>.withKey(keyFlow: StateFlow<K?>): PagedReplica<T> {
    return WithKeyReplica(this, keyFlow)
}

private class WithKeyReplica<K : Any, T : Any>(
    private val keyedReplica: KeyedPagedReplica<K, T>,
    private val keyFlow: StateFlow<K?>
) : PagedReplica<T> {

    override fun observe(observerHost: ReplicaObserverHost): PagedReplicaObserver<T> {
        return keyedReplica.observe(observerHost, keyFlow)
    }

    override fun refresh() {
        val key = keyFlow.value ?: return
        keyedReplica.refresh(key)
    }

    override fun revalidate() {
        val key = keyFlow.value ?: return
        keyedReplica.revalidate(key)
    }

    override fun loadNext() {
        val key = keyFlow.value ?: return
        keyedReplica.loadNext(key)
    }

    override fun loadPrevious() {
        val key = keyFlow.value ?: return
        keyedReplica.loadPrevious(key)
    }
}