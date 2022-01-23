package me.aartikov.replica.devtools.dto

data class ReplicaClientDto(
    val replicas: MutableMap<String, ReplicaDto> = mutableMapOf(),
    val keyedReplicas: MutableMap<String, KeyedReplicaDto> = mutableMapOf()
)