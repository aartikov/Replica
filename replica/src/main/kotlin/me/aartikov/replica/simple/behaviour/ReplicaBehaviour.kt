package me.aartikov.replica.simple.behaviour

import me.aartikov.replica.simple.CoreReplica
import me.aartikov.replica.simple.ReplicaEvent

interface ReplicaBehaviour<T : Any> {

    fun setup(replica: CoreReplica<T>) {}

    fun handleEvent(replica: CoreReplica<T>, event: ReplicaEvent<T>)
}