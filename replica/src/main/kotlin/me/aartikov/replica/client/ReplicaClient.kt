package me.aartikov.replica.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import me.aartikov.replica.client.ReplicaClient.Companion.GlobalCoroutineScope
import me.aartikov.replica.simple.CoreReplica
import me.aartikov.replica.simple.Fetcher
import me.aartikov.replica.simple.ReplicaSettings
import me.aartikov.replica.simple.behaviour.ReplicaBehaviour

interface ReplicaClient {

    companion object {
        val GlobalCoroutineScope = CoroutineScope(
            SupervisorJob() + Dispatchers.Main.immediate
        )
    }

    val coroutineScope: CoroutineScope

    val replicaSettings: ReplicaSettings

    fun <T : Any> createReplica(
        settings: ReplicaSettings = this.replicaSettings,
        behaviours: List<ReplicaBehaviour<T>> = emptyList(),
        coroutineScope: CoroutineScope = this.coroutineScope,
        fetcher: Fetcher<T>
    ): CoreReplica<T>

}

fun ReplicaClient(
    replicaSettings: ReplicaSettings = ReplicaSettings.Default,
    coroutineScope: CoroutineScope = GlobalCoroutineScope
): ReplicaClient {
    return ReplicaClientImpl(replicaSettings, coroutineScope)
}