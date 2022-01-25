package me.aartikov.replica.single.behaviour.standard

import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.network.NetworkConnectivityProvider
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.behaviour.ReplicaBehaviour

class DoOnNetworkConnectivityChanged<T : Any>(
    private val networkConnectivityProvider: NetworkConnectivityProvider,
    private val action: suspend PhysicalReplica<T>.(connected: Boolean) -> Unit
) : ReplicaBehaviour<T> {

    override fun setup(replica: PhysicalReplica<T>) {
        networkConnectivityProvider.connectedFlow
            .drop(1)
            .onEach {
                replica.action(it)
            }
            .launchIn(replica.coroutineScope)
    }
}