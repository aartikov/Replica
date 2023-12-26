package me.aartikov.replica.paged.behaviour.standard

import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.network.NetworkConnectivityProvider
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PhysicalPagedReplica
import me.aartikov.replica.paged.behaviour.PagedReplicaBehaviour

/**
 * [PagedReplicaBehaviour] that executes some [action] when network connectivity status changed.
 */
class DoOnNetworkConnectivityChanged<T : Any, P : Page<T>>(
    private val networkConnectivityProvider: NetworkConnectivityProvider,
    private val action: suspend PhysicalPagedReplica<T, P>.(connected: Boolean) -> Unit
) : PagedReplicaBehaviour<T, P> {

    override fun setup(replica: PhysicalPagedReplica<T, P>) {
        networkConnectivityProvider.connectedFlow
            .drop(1)
            .onEach {
                replica.action(it)
            }
            .launchIn(replica.coroutineScope)
    }
}