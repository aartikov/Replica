package me.aartikov.replica.algebra

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.keyed_paged.KeyedPagedReplica
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedReplica
import me.aartikov.replica.paged.PagedReplicaObserver

/**
 * Converts [KeyedPagedReplica] to [PagedReplica] by fixing a key.
 */
fun <K : Any, T : Any, P : Page<T>> KeyedPagedReplica<K, T, P>.withKey(key: K): PagedReplica<T, P> {
    return WithKeyPagedReplica(this, MutableStateFlow(key))
}

/**
 * Converts [KeyedPagedReplica] to [PagedReplica] by passing [StateFlow] with dynamic key.
 */
fun <K : Any, T : Any, P : Page<T>> KeyedPagedReplica<K, T, P>.withKey(keyFlow: StateFlow<K?>): PagedReplica<T, P> {
    return WithKeyPagedReplica(this, keyFlow)
}

private class WithKeyPagedReplica<K : Any, T : Any, P : Page<T>>(
    private val keyedPagedReplica: KeyedPagedReplica<K, T, P>,
    private val keyFlow: StateFlow<K?>
) : PagedReplica<T, P> {

    override fun observe(
        observerCoroutineScope: CoroutineScope,
        observerActive: StateFlow<Boolean>
    ): PagedReplicaObserver<T, P> {
        return keyedPagedReplica.observe(
            observerCoroutineScope,
            observerActive,
            keyFlow
        )
    }

    override fun refresh() {
        val key = keyFlow.value ?: return
        keyedPagedReplica.refresh(key)
    }

    override fun revalidate() {
        val key = keyFlow.value ?: return
        keyedPagedReplica.revalidate(key)
    }

    override fun loadNext() {
        val key = keyFlow.value ?: return
        keyedPagedReplica.loadNext(key)
    }

    override fun loadPrevious() {
        val key = keyFlow.value ?: return
        keyedPagedReplica.loadPrevious(key)
    }
}