package me.aartikov.replica.paged.behaviour.standard

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedPhysicalReplica
import me.aartikov.replica.paged.PagedReplicaEvent
import me.aartikov.replica.paged.behaviour.PagedReplicaBehaviour
import me.aartikov.replica.single.ReplicaEvent

/**
 * [PagedReplicaBehaviour] that executes some [action] on all [ReplicaEvent].
 */
class PagedDoOnEvent<I : Any, P : Page<I>>(
    private val action: suspend PagedPhysicalReplica<I, P>.(event: PagedReplicaEvent<I, P>) -> Unit
) : PagedReplicaBehaviour<I, P> {

    override fun setup(replica: PagedPhysicalReplica<I, P>) {
        replica.eventFlow
            .onEach { event ->
                replica.action(event)
            }
            .launchIn(replica.coroutineScope)
    }
}