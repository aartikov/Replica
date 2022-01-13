package me.aartikov.replica.client

import me.aartikov.replica.keyed.KeyedPhysicalReplica
import me.aartikov.replica.single.PhysicalReplica

sealed interface ReplicaClientEvent {
    class ReplicaCreated(val replica: PhysicalReplica<*>) : ReplicaClientEvent
    class KeyedReplicaCreated(val keyedReplica: KeyedPhysicalReplica<*, *>) : ReplicaClientEvent
}