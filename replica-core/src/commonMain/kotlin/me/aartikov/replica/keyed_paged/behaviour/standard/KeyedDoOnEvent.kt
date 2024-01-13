package me.aartikov.replica.keyed_paged.behaviour.standard

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.keyed_paged.KeyedPagedPhysicalReplica
import me.aartikov.replica.keyed_paged.KeyedPagedReplicaEvent
import me.aartikov.replica.keyed_paged.behaviour.KeyedPagedReplicaBehaviour
import me.aartikov.replica.paged.Page

/**
 * [KeyedPagedReplicaBehaviour] that executes some [action] on all [KeyedPagedReplicaEvent].
 */
class KeyedPagedPagedDoOnEvent<K : Any, T : Any, P : Page<T>>(
    private val action: suspend KeyedPagedPhysicalReplica<K, T, P>.(event: KeyedPagedReplicaEvent<K, T, P>) -> Unit
) : KeyedPagedReplicaBehaviour<K, T, P> {

    override fun setup(keyedPagedReplica: KeyedPagedPhysicalReplica<K, T, P>) {
        keyedPagedReplica.eventFlow
            .onEach { event ->
                keyedPagedReplica.action(event)
            }
            .launchIn(keyedPagedReplica.coroutineScope)
    }
}