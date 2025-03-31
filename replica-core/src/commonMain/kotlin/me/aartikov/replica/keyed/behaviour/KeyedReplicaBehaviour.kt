package me.aartikov.replica.keyed.behaviour

import me.aartikov.replica.keyed.KeyedPhysicalReplica
import me.aartikov.replica.keyed.behaviour.standard.doOnEvent

/**
 * Allows to add a custom behavior to a keyed replica.
 * See also: [doOnEvent]
 */
interface KeyedReplicaBehaviour<K : Any, T : Any> {

    companion object

    fun setup(keyedReplica: KeyedPhysicalReplica<K, T>)
}