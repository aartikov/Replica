package me.aartikov.replica.keyed_paged.behaviour

import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.keyed_paged.KeyedPagedPhysicalReplica
import me.aartikov.replica.keyed_paged.behaviour.standard.doOnEvent
import me.aartikov.replica.paged.Page

/**
 * Allows to add a custom behavior to a keyed paged replica.
 * See also: [doOnEvent]
 */
interface KeyedPagedReplicaBehaviour<K : Any, I : Any, P : Page<I>> {

    companion object

    fun setup(replicaClient: ReplicaClient, keyedPagedReplica: KeyedPagedPhysicalReplica<K, I, P>)
}