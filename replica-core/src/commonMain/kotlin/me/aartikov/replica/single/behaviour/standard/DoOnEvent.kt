package me.aartikov.replica.single.behaviour.standard

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaEvent
import me.aartikov.replica.single.behaviour.ReplicaBehaviour

/**
 * [ReplicaBehaviour] that executes some [action] on all [ReplicaEvent].
 */
fun <T : Any> ReplicaBehaviour.Companion.doOnEvent(
    action: suspend PhysicalReplica<T>.(event: ReplicaEvent<T>) -> Unit
): ReplicaBehaviour<T> = DoOnEvent(action)

private class DoOnEvent<T : Any>(
    private val action: suspend PhysicalReplica<T>.(event: ReplicaEvent<T>) -> Unit
) : ReplicaBehaviour<T> {

    override fun setup(replicaClient: ReplicaClient, replica: PhysicalReplica<T>) {
        replica.eventFlow
            .onEach { event ->
                replica.action(event)
            }
            .launchIn(replica.coroutineScope)
    }
}