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
        coroutineScope: CoroutineScope = this.coroutineScope,
        replicaSettings: ReplicaSettings = this.replicaSettings,
        behaviours: List<ReplicaBehaviour<T>> = emptyList(),
        fetcher: Fetcher<T>
    ): CoreReplica<T>

}

fun ReplicaClient(
    coroutineScope: CoroutineScope = GlobalCoroutineScope,
    replicaSettings: ReplicaSettings = ReplicaSettings.Default
): ReplicaClient {
    return ReplicaClientImpl(coroutineScope, replicaSettings)
}