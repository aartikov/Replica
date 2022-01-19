package me.aartikov.replica.devtools.internal

import me.aartikov.replica.common.ReplicaId
import me.aartikov.replica.keyed.KeyedReplicaState

data class KeyedReplicaInfo(
    val id: ReplicaId,
    val name: String,
    var state: KeyedReplicaState,
    val childInfos: MutableMap<ReplicaId, ReplicaInfo> = mutableMapOf()
)