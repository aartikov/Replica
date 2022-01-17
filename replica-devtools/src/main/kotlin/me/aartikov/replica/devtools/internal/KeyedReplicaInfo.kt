package me.aartikov.replica.devtools.internal

import me.aartikov.replica.keyed.KeyedReplicaId
import me.aartikov.replica.keyed.KeyedReplicaState
import me.aartikov.replica.single.ReplicaId

data class KeyedReplicaInfo(
    val id: KeyedReplicaId,
    val name: String,
    var state: KeyedReplicaState,
    val childInfos: MutableMap<ReplicaId, ReplicaInfo> = mutableMapOf()
)