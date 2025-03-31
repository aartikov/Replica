package me.aartikov.replica.paged.behaviour.standard

import kotlinx.coroutines.launch
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedPhysicalReplica
import me.aartikov.replica.paged.behaviour.PagedReplicaBehaviour

/**
 * [PagedReplicaBehaviour] that executes some [action] when a replica is created.
 */
fun <I : Any, P : Page<I>> PagedReplicaBehaviour.Companion.doOnCreated(
    action: suspend PagedPhysicalReplica<I, P>.() -> Unit
): PagedReplicaBehaviour<I, P> = DoOnCreated(action)

private class DoOnCreated<I : Any, P : Page<I>>(
    private val action: suspend PagedPhysicalReplica<I, P>.() -> Unit
) : PagedReplicaBehaviour<I, P> {

    override fun setup(replicaClient: ReplicaClient, pagedReplica: PagedPhysicalReplica<I, P>) {
        pagedReplica.coroutineScope.launch {
            pagedReplica.action()
        }
    }
}