package me.aartikov.replica.keyed.behaviour.standard

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.keyed.KeyedPhysicalReplica
import me.aartikov.replica.keyed.KeyedReplicaEvent
import me.aartikov.replica.keyed.behaviour.KeyedReplicaBehaviour

/**
 * [KeyedReplicaBehaviour] that executes some [action] on all [KeyedReplicaEvent].
 */
class KeyedDoOnEvent<K : Any, T : Any>(
    private val action: suspend KeyedPhysicalReplica<K, T>.(event: KeyedReplicaEvent<K, T>) -> Unit
) : KeyedReplicaBehaviour<K, T> {

    override fun setup(keyedReplica: KeyedPhysicalReplica<K, T>) {
        keyedReplica.eventFlow
            .onEach { event ->
                keyedReplica.action(event)
            }
            .launchIn(keyedReplica.coroutineScope)
    }
}