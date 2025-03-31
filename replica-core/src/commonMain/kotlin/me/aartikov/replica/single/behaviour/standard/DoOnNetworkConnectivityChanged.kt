package me.aartikov.replica.single.behaviour.standard

import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.behaviour.ReplicaBehaviour

/**
 * [ReplicaBehaviour] that executes some [action] when network connectivity status changed.
 */
fun <T : Any> ReplicaBehaviour.Companion.doOnNetworkConnectivityChanged(
    action: suspend PhysicalReplica<T>.(connected: Boolean) -> Unit
): ReplicaBehaviour<T> = DoOnNetworkConnectivityChanged(action)

private class DoOnNetworkConnectivityChanged<T : Any>(
    private val action: suspend PhysicalReplica<T>.(connected: Boolean) -> Unit
) : ReplicaBehaviour<T> {

    override fun setup(replicaClient: ReplicaClient, replica: PhysicalReplica<T>) {
        val networkConnectivityProvider = replicaClient.networkConnectivityProvider ?: return

        networkConnectivityProvider.connectedFlow
            .drop(1)
            .onEach {
                replica.action(it)
            }
            .launchIn(replica.coroutineScope)
    }
}