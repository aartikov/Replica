package me.aartikov.replica.devtools.dto

import kotlinx.serialization.Serializable

@Serializable
sealed class DevToolsEventDto

@Serializable
data class ReplaceAll(val replicaClient: ReplicaClientDto) : DevToolsEventDto()

@Serializable
data class ReplicaCreated(val replica: ReplicaDto) : DevToolsEventDto()

@Serializable
data class KeyedReplicaCreated(val replica: KeyedReplicaDto) : DevToolsEventDto()

@Serializable
data class ReplicaUpdated(val id: String, val state: ReplicaStateDto) : DevToolsEventDto()

@Serializable
data class KeyedReplicaChildUpdated(
    val keyedReplicaId: String,
    val childReplicaId: String,
    val state: ReplicaStateDto
) : DevToolsEventDto()

@Serializable
data class KeyedReplicaUpdated(
    val id: String,
    val state: KeyedReplicaStateDto
) : DevToolsEventDto()