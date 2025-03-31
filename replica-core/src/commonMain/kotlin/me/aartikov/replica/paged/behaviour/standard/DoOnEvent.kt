package me.aartikov.replica.paged.behaviour.standard

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedPhysicalReplica
import me.aartikov.replica.paged.PagedReplicaEvent
import me.aartikov.replica.paged.behaviour.PagedReplicaBehaviour
import me.aartikov.replica.single.ReplicaEvent

/**
 * [PagedReplicaBehaviour] that executes some [action] on every [ReplicaEvent].
 */
fun <I : Any, P : Page<I>> PagedReplicaBehaviour.Companion.doOnEvent(
    action: suspend PagedPhysicalReplica<I, P>.(event: PagedReplicaEvent<I, P>) -> Unit
): PagedReplicaBehaviour<I, P> = DoOnEvent(action)

private class DoOnEvent<I : Any, P : Page<I>>(
    private val action: suspend PagedPhysicalReplica<I, P>.(event: PagedReplicaEvent<I, P>) -> Unit
) : PagedReplicaBehaviour<I, P> {

    override fun setup(replicaClient: ReplicaClient, pagedReplica: PagedPhysicalReplica<I, P>) {
        pagedReplica.eventFlow
            .onEach { event ->
                pagedReplica.action(event)
            }
            .launchIn(pagedReplica.coroutineScope)
    }
}