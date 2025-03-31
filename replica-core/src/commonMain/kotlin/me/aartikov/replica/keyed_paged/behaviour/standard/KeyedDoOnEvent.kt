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
fun <K : Any, I : Any, P : Page<I>> KeyedPagedReplicaBehaviour.Companion.doOnEvent(
    action: suspend KeyedPagedPhysicalReplica<K, I, P>.(event: KeyedPagedReplicaEvent<K, I, P>) -> Unit
): KeyedPagedReplicaBehaviour<K, I, P> = DoOnEvent(action)

private class DoOnEvent<K : Any, I : Any, P : Page<I>>(
    private val action: suspend KeyedPagedPhysicalReplica<K, I, P>.(event: KeyedPagedReplicaEvent<K, I, P>) -> Unit
) : KeyedPagedReplicaBehaviour<K, I, P> {

    override fun setup(keyedPagedReplica: KeyedPagedPhysicalReplica<K, I, P>) {
        keyedPagedReplica.eventFlow
            .onEach { event ->
                keyedPagedReplica.action(event)
            }
            .launchIn(keyedPagedReplica.coroutineScope)
    }
}