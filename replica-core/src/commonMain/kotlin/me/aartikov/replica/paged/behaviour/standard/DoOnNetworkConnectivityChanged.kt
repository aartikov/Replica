package me.aartikov.replica.paged.behaviour.standard

import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedPhysicalReplica
import me.aartikov.replica.paged.behaviour.PagedReplicaBehaviour

/**
 * [PagedReplicaBehaviour] that executes some [action] when network connectivity status changed.
 */
fun <I : Any, P : Page<I>> PagedReplicaBehaviour.Companion.doOnNetworkConnectivityChanged(
    action: suspend PagedPhysicalReplica<I, P>.(connected: Boolean) -> Unit
): PagedReplicaBehaviour<I, P> = DoOnNetworkConnectivityChanged(action)

private class DoOnNetworkConnectivityChanged<I : Any, P : Page<I>>(
    private val action: suspend PagedPhysicalReplica<I, P>.(connected: Boolean) -> Unit
) : PagedReplicaBehaviour<I, P> {

    override fun setup(replicaClient: ReplicaClient, pagedReplica: PagedPhysicalReplica<I, P>) {
        val networkConnectivityProvider = replicaClient.networkConnectivityProvider ?: return

        networkConnectivityProvider.connectedFlow
            .drop(1)
            .onEach {
                pagedReplica.action(it)
            }
            .launchIn(pagedReplica.coroutineScope)
    }
}