package me.aartikov.replica.devtools.internal

import me.aartikov.replica.common.ReplicaId
import me.aartikov.replica.common.ReplicaTag
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.single.ReplicaState

data class ReplicaInfo(
    val id: ReplicaId,
    val name: String,
    val settings: ReplicaSettings,
    val tags: Set<ReplicaTag>,
    var state: ReplicaState<*>
)