package me.aartikov.replica.single.behaviour

import me.aartikov.replica.single.PhysicalReplica

interface ReplicaBehaviour<T : Any> {

    fun setup(replica: PhysicalReplica<T>)
}