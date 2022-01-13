package me.aartikov.replica.keyed

import me.aartikov.replica.single.PhysicalReplica

sealed interface KeyedReplicaEvent<K : Any, T : Any> {

    class ReplicaCreated<K : Any, T : Any>(
        val key: K,
        val replica: PhysicalReplica<T>
    ) : KeyedReplicaEvent<K, T>

    class ReplicaRemoved<K : Any, T : Any>(
        val key: K,
        val replica: PhysicalReplica<T>
    ) : KeyedReplicaEvent<K, T>
}