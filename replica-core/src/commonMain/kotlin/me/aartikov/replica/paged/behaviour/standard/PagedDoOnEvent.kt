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
class PagedDoOnEvent<T : Any, P : Page<T>>(
    private val action: suspend PagedPhysicalReplica<T, P>.(event: PagedReplicaEvent<T, P>) -> Unit
) : PagedReplicaBehaviour<T, P> {

    override fun setup(replica: PagedPhysicalReplica<T, P>) {
        replica.eventFlow
            .onEach { event ->
                replica.action(event)
            }
            .launchIn(replica.coroutineScope)
    }
}