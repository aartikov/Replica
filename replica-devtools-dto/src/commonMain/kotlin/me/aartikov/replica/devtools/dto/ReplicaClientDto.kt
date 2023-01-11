package me.aartikov.replica.devtools.dto

import kotlinx.serialization.Serializable

@Serializable
data class ReplicaClientDto(
    val replicas: Map<Long, ReplicaDto> = emptyMap(),
    val keyedReplicas: Map<Long, KeyedReplicaDto> = emptyMap()
)