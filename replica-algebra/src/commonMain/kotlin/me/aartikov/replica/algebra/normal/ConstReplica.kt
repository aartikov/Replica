package me.aartikov.replica.algebra.normal

import kotlinx.coroutines.flow.MutableStateFlow
import me.aartikov.replica.single.Replica

/**
 * Creates Replica with a const data.
 */
fun <T : Any> constReplica(data: T?): Replica<T> {
    return flowReplica(MutableStateFlow(data))
}