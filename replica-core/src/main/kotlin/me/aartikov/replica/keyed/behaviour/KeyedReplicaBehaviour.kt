package me.aartikov.replica.keyed.behaviour

import me.aartikov.replica.keyed.KeyedPhysicalReplica

interface KeyedReplicaBehaviour<K : Any, T : Any> {

    fun setup(keyedReplica: KeyedPhysicalReplica<K, T>)
}