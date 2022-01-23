package me.aartikov.replica.devtools.dto

import kotlinx.serialization.Serializable

@Serializable
data class KeyedReplicaDto(
    val id: String,
    val name: String,
    var state: KeyedReplicaStateDto,
    val childReplicas: MutableMap<String, ReplicaDto> = mutableMapOf()
)

@Serializable
data class KeyedReplicaStateDto(
    val replicaCount: Int,
    val replicaWithObserversCount: Int,
    val replicaWithActiveObserversCount: Int
)