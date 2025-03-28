package me.aartikov.replica.decompose

import com.arkivanov.essenty.lifecycle.Lifecycle
import me.aartikov.replica.paged.PagedReplica
import me.aartikov.replica.paged.PagedReplicaObserver

/**
 * Observes [PagedReplica] in a a scope of [Lifecycle].
 */
@Deprecated("Use Lifecycle.replicaObserverHost()")
fun <T : Any> PagedReplica<T>.observe(lifecycle: Lifecycle): PagedReplicaObserver<T> {
    return this.observe(lifecycle.replicaObserverHost())
}