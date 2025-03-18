package me.aartikov.replica.decompose

import com.arkivanov.essenty.lifecycle.Lifecycle
import me.aartikov.replica.common.ReplicaObserverHost


fun Lifecycle.replicaObserverHost(): ReplicaObserverHost {
    return ReplicaObserverHost(coroutineScope(), activeFlow())
}