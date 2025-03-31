package me.aartikov.replica.paged.behaviour.standard

import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.network.NetworkConnectivityProvider
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedPhysicalReplica
import me.aartikov.replica.paged.behaviour.PagedReplicaBehaviour

/**
 * [PagedReplicaBehaviour] that executes some [action] when network connectivity status changed.
 */
fun <I : Any, P : Page<I>> PagedReplicaBehaviour.Companion.doOnNetworkConnectivityChanged(
    networkConnectivityProvider: NetworkConnectivityProvider,
    action: suspend PagedPhysicalReplica<I, P>.(connected: Boolean) -> Unit
): PagedReplicaBehaviour<I, P> = DoOnNetworkConnectivityChanged(networkConnectivityProvider, action)

private class DoOnNetworkConnectivityChanged<I : Any, P : Page<I>>(
    private val networkConnectivityProvider: NetworkConnectivityProvider,
    private val action: suspend PagedPhysicalReplica<I, P>.(connected: Boolean) -> Unit
) : PagedReplicaBehaviour<I, P> {

    override fun setup(replica: PagedPhysicalReplica<I, P>) {
        networkConnectivityProvider.connectedFlow
            .drop(1)
            .onEach {
                replica.action(it)
            }
            .launchIn(replica.coroutineScope)
    }
}