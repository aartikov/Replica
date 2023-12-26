package me.aartikov.replica.paged.behaviour.standard

import kotlinx.coroutines.launch
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PhysicalPagedReplica
import me.aartikov.replica.paged.behaviour.PagedReplicaBehaviour

/**
 * [PagedReplicaBehaviour] that executes some [action] when a replica is created.
 */
class DoOnCreated<T : Any, P : Page<T>>(
    private val action: suspend PhysicalPagedReplica<T, P>.() -> Unit
) : PagedReplicaBehaviour<T, P> {

    override fun setup(replica: PhysicalPagedReplica<T, P>) {
        replica.coroutineScope.launch {
            replica.action()
        }
    }
}