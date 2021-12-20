package me.aartikov.replica.keyed.internal

import kotlinx.coroutines.CoroutineScope
import me.aartikov.replica.single.PhysicalReplica

internal class KeyedReplicaElement<T : Any>(
    val coroutineScope: CoroutineScope,
    val replica: PhysicalReplica<T>
)