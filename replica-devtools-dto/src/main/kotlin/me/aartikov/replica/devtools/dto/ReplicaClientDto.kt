package me.aartikov.replica.devtools.dto

import kotlinx.serialization.Serializable

@Serializable
data class ReplicaClientDto(
    val replicas: Map<String, ReplicaDto> = emptyMap(),
    val keyedReplicas: Map<String, KeyedReplicaDto> = emptyMap()
)