package me.aartikov.replica.decompose

import com.arkivanov.essenty.lifecycle.Lifecycle
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import me.aartikov.replica.common.ReplicaObserverHost


fun Lifecycle.replicaObserverHost(
    dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate
): ReplicaObserverHost {
    return ReplicaObserverHost(coroutineScope(dispatcher), activeFlow())
}