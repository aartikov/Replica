package me.aartikov.replica.devtools.internal

import me.aartikov.replica.single.ReplicaId
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.single.ReplicaState

data class ReplicaInfo(
    val id: ReplicaId,
    val name: String,
    val settings: ReplicaSettings,
    var state: ReplicaState<*>
)