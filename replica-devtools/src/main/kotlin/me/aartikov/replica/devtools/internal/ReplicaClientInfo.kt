package me.aartikov.replica.devtools.internal

import me.aartikov.replica.common.ReplicaId

data class ReplicaClientInfo(
    val replicaInfos: MutableMap<ReplicaId, ReplicaInfo> = mutableMapOf(),
    val keyedReplicaInfos: MutableMap<ReplicaId, KeyedReplicaInfo> = mutableMapOf()
)