package me.aartikov.replica.paged.behaviour.standard

import kotlinx.coroutines.launch
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedPhysicalReplica
import me.aartikov.replica.paged.behaviour.PagedReplicaBehaviour

/**
 * [PagedReplicaBehaviour] that executes some [action] when a replica is created.
 */
class DoOnCreated<T : Any, P : Page<T>>(
    private val action: suspend PagedPhysicalReplica<T, P>.() -> Unit
) : PagedReplicaBehaviour<T, P> {

    override fun setup(replica: PagedPhysicalReplica<T, P>) {
        replica.coroutineScope.launch {
            replica.action()
        }
    }
}