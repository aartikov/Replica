package me.aartikov.replica.paged.behaviour.standard

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedReplicaEvent
import me.aartikov.replica.paged.PhysicalPagedReplica
import me.aartikov.replica.paged.behaviour.PagedReplicaBehaviour
import me.aartikov.replica.single.ReplicaEvent

/**
 * [PagedReplicaBehaviour] that executes some [action] on all [ReplicaEvent].
 */
class DoOnEvent<T : Any, P : Page<T>>(
    private val action: suspend PhysicalPagedReplica<T, P>.(event: PagedReplicaEvent<T, P>) -> Unit
) : PagedReplicaBehaviour<T, P> {

    override fun setup(replica: PhysicalPagedReplica<T, P>) {
        replica.eventFlow
            .onEach { event ->
                replica.action(event)
            }
            .launchIn(replica.coroutineScope)
    }
}