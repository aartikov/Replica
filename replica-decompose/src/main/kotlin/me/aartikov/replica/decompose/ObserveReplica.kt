package me.aartikov.replica.decompose

import com.arkivanov.essenty.lifecycle.Lifecycle
import me.aartikov.replica.single.Replica
import me.aartikov.replica.single.ReplicaObserver

fun <T : Any> Replica<T>.observe(lifecycle: Lifecycle): ReplicaObserver<T> {
    return this.observe(
        lifecycle.coroutineScope(),
        lifecycle.activeFlow()
    )
}