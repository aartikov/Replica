package me.aartikov.replica.decompose

import com.arkivanov.essenty.lifecycle.Lifecycle
import me.aartikov.replica.single.Replica
import me.aartikov.replica.single.ReplicaObserver

/**
 * Observes [Replica] in a a scope of [Lifecycle].
 */
@Deprecated("Use Lifecycle.replicaObserverHost()")
fun <T : Any> Replica<T>.observe(lifecycle: Lifecycle): ReplicaObserver<T> {
    return this.observe(lifecycle.replicaObserverHost())
}