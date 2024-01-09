package me.aartikov.replica.decompose

import com.arkivanov.essenty.lifecycle.Lifecycle
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedReplica
import me.aartikov.replica.paged.PagedReplicaObserver

/**
 * Observes [PagedReplica] in a a scope of [Lifecycle].
 */
fun <T : Any, P : Page<T>> PagedReplica<T, P>.observe(lifecycle: Lifecycle): PagedReplicaObserver<T, P> {
    return this.observe(
        lifecycle.coroutineScope(),
        lifecycle.activeFlow()
    )
}