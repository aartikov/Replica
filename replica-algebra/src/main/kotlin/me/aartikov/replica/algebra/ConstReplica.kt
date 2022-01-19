package me.aartikov.replica.algebra

import kotlinx.coroutines.flow.MutableStateFlow
import me.aartikov.replica.single.Replica

fun <T : Any> constReplica(data: T): Replica<T> {
    return stateFlowReplica(MutableStateFlow(data))
}