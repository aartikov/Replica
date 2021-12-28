package me.aartikov.replica.single.behaviour

import kotlinx.coroutines.CoroutineScope
import me.aartikov.replica.single.PhysicalReplica

interface ReplicaBehaviour<T : Any> {

    fun setup(coroutineScope: CoroutineScope, replica: PhysicalReplica<T>)
}