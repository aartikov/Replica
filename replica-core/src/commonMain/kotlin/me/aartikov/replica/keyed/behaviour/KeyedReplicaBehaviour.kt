package me.aartikov.replica.keyed.behaviour

import me.aartikov.replica.keyed.KeyedPhysicalReplica

/**
 * Allows to add a custom behavior to a keyed replica.
 */
interface KeyedReplicaBehaviour<K : Any, T : Any> {

    fun setup(keyedReplica: KeyedPhysicalReplica<K, T>)
}