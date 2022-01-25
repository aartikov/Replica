package me.aartikov.replica.devtools.dto

import kotlinx.serialization.Serializable

@Serializable
sealed class ReplicaEventDto

@Serializable
data class ReplaceAll(val replicaClient: ReplicaClientDto) : ReplicaEventDto()

@Serializable
data class ReplicaCreated(val replica: ReplicaDto) : ReplicaEventDto()

@Serializable
data class KeyedReplicaCreated(val replica: KeyedReplicaDto) : ReplicaEventDto()

@Serializable
data class ReplicaUpdated(val id: String, val state: ReplicaStateDto) : ReplicaEventDto()

@Serializable
data class KeyedReplicaUpdated(
    val id: String,
    val state: KeyedReplicaStateDto
) : ReplicaEventDto()