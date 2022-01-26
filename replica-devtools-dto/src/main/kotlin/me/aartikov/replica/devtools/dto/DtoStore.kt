package me.aartikov.replica.devtools.dto

import kotlinx.coroutines.flow.asSharedFlow

class DtoStore(
    val onDtoChanged: (ReplicaClientDto) -> Unit
) {

    private val dto = ReplicaClientDto()

    private val _eventFlow =
        kotlinx.coroutines.flow.MutableSharedFlow<DevToolsEventDto>(extraBufferCapacity = 1000)
    val eventFlow: kotlinx.coroutines.flow.Flow<DevToolsEventDto> = _eventFlow.asSharedFlow()

    val lastState
        get() = dto

    fun addReplica(replica: ReplicaDto) {
        dto.replicas[replica.id] = replica
        _eventFlow.tryEmit(ReplicaCreated(replica))
        onDtoChanged(dto)
    }

    fun addKeyedReplica(keyedReplica: KeyedReplicaDto) {
        dto.keyedReplicas[keyedReplica.id] = keyedReplica
        _eventFlow.tryEmit(KeyedReplicaCreated(keyedReplica))
        onDtoChanged(dto)
    }

    fun addKeyedReplicaChild(keyedReplicaId: String, childReplica: ReplicaDto) {
        dto.keyedReplicas[keyedReplicaId]?.childReplicas?.put(
            childReplica.id,
            childReplica
        )
        onDtoChanged(dto)
    }

    fun updateReplicaState(replicaId: String, state: ReplicaStateDto) {
        dto.replicas[replicaId]?.state = state
        _eventFlow.tryEmit(ReplicaUpdated(replicaId, state))
        onDtoChanged(dto)
    }

    fun updateKeyedReplicaState(keyedReplicaId: String, state: KeyedReplicaStateDto) {
        dto.keyedReplicas[keyedReplicaId]?.state = state
        _eventFlow.tryEmit(KeyedReplicaUpdated(keyedReplicaId, state))
        onDtoChanged(dto)
    }

    fun updateKeyedReplicaChildState(
        keyedReplicaId: String,
        childReplicaId: String,
        state: ReplicaStateDto
    ) {
        dto.keyedReplicas[keyedReplicaId]
            ?.childReplicas?.get(childReplicaId)?.state = state
        _eventFlow.tryEmit(
            KeyedReplicaChildUpdated(
                keyedReplicaId,
                childReplicaId,
                state
            )
        )
        onDtoChanged(dto)
    }

    fun removeKeyedReplicaChild(keyedReplicaId: String, childReplicaId: String) {
        dto.keyedReplicas[keyedReplicaId]?.childReplicas?.remove(childReplicaId)
        onDtoChanged(dto)
    }

    fun updateState(state: ReplicaClientDto) {
        dto.keyedReplicas.apply {
            clear()
            putAll(state.keyedReplicas)
        }
        dto.replicas.apply {
            clear()
            putAll(state.replicas)
        }
        onDtoChanged(dto)
    }
}