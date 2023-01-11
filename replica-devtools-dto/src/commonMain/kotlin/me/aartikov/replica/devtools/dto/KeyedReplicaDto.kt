package me.aartikov.replica.devtools.dto

import kotlinx.serialization.Serializable

@Serializable
data class KeyedReplicaDto(
    val id: Long,
    val name: String,
    val state: KeyedReplicaStateDto,
    val childReplicas: Map<Long, ReplicaDto> = emptyMap()
)

@Serializable
data class KeyedReplicaStateDto(
    val replicaCount: Int,
    val replicaWithObserversCount: Int,
    val replicaWithActiveObserversCount: Int
)