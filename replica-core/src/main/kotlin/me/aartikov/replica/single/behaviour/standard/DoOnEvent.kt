package me.aartikov.replica.single.behaviour.standard

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaEvent
import me.aartikov.replica.single.behaviour.ReplicaBehaviour

class DoOnEvent<T : Any>(
    private val action: suspend PhysicalReplica<T>.(event: ReplicaEvent<T>) -> Unit
) : ReplicaBehaviour<T> {

    override fun setup(replica: PhysicalReplica<T>) {
        replica.eventFlow
            .onEach { event ->
                replica.action(event)
            }
            .launchIn(replica.coroutineScope)
    }
}