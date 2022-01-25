package me.aartikov.replica.devtools.dto

import kotlinx.serialization.Serializable

@Serializable
data class ReplicaClientDto(
    val replicas: MutableMap<String, ReplicaDto> = mutableMapOf(),
    val keyedReplicas: MutableMap<String, KeyedReplicaDto> = mutableMapOf()
)