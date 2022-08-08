package me.aartikov.replica.decompose

import com.arkivanov.essenty.lifecycle.Lifecycle
import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.keyed.KeyedReplica
import me.aartikov.replica.single.ReplicaObserver

/**
 * Observes [KeyedReplica] in a scope of [Lifecycle].
 */
fun <T : Any, K : Any> KeyedReplica<K, T>.observe(
    lifecycle: Lifecycle,
    key: StateFlow<K?>
): ReplicaObserver<T> {
    return observe(
        lifecycle.coroutineScope(),
        lifecycle.activeFlow(),
        key
    )
}