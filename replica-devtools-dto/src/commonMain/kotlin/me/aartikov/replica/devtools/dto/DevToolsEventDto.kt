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
data class ReplicaUpdated(val id: Long, val state: ReplicaStateDto) : DevToolsEventDto()

@Serializable
data class KeyedReplicaChildCreated(
    val keyedReplicaId: Long,
    val childReplica: ReplicaDto
) : DevToolsEventDto()

@Serializable
data class KeyedReplicaChildUpdated(
    val keyedReplicaId: Long,
    val childReplicaId: Long,
    val state: ReplicaStateDto
) : DevToolsEventDto()

@Serializable
data class KeyedReplicaUpdated(
    val id: Long,
    val state: KeyedReplicaStateDto
) : DevToolsEventDto()

@Serializable
data class KeyedReplicaChildRemoved(
    val keyedReplicaId: Long,
    val childReplicaId: Long
) : DevToolsEventDto()