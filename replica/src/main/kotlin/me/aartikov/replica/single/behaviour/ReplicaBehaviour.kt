package me.aartikov.replica.single.behaviour

import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaEvent

interface ReplicaBehaviour<T : Any> {

    fun setup(replica: PhysicalReplica<T>) {}

    fun handleEvent(replica: PhysicalReplica<T>, event: ReplicaEvent<T>)
}