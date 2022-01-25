package me.aartikov.replica.single.behaviour.standard

import kotlinx.coroutines.launch
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.behaviour.ReplicaBehaviour

class DoOnCreated<T : Any>(
    private val action: suspend PhysicalReplica<T>.() -> Unit
) : ReplicaBehaviour<T> {

    override fun setup(replica: PhysicalReplica<T>) {
        replica.coroutineScope.launch {
            replica.action()
        }
    }
}