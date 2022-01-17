package me.aartikov.replica.devtools.internal

import me.aartikov.replica.keyed.KeyedReplicaId
import me.aartikov.replica.single.ReplicaId

data class ReplicaClientInfo(
    val replicaInfos: MutableMap<ReplicaId, ReplicaInfo> = mutableMapOf(),
    val keyedReplicaInfos: MutableMap<KeyedReplicaId, KeyedReplicaInfo> = mutableMapOf()
)