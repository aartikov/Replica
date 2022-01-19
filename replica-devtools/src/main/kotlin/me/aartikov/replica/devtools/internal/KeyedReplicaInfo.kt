package me.aartikov.replica.devtools.internal

import me.aartikov.replica.common.ReplicaId
import me.aartikov.replica.common.ReplicaTag
import me.aartikov.replica.keyed.KeyedReplicaSettings
import me.aartikov.replica.keyed.KeyedReplicaState

data class KeyedReplicaInfo(
    val id: ReplicaId,
    val name: String,
    val settings: KeyedReplicaSettings<*, *>,
    val tags: Set<ReplicaTag>,
    var state: KeyedReplicaState,
    val childInfos: MutableMap<ReplicaId, ReplicaInfo> = mutableMapOf()
)