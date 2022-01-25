package me.aartikov.replica.devtools.internal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import me.aartikov.replica.common.ReplicaId
import me.aartikov.replica.devtools.dto.*
import me.aartikov.replica.keyed.KeyedPhysicalReplica
import me.aartikov.replica.keyed.KeyedReplicaState
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaState

class DtoStore(
    val onDtoChanged: (ReplicaClientDto) -> Unit
) {

    private val dto = ReplicaClientDto()

    private val _eventFlow = MutableSharedFlow<DevToolsEventDto>(extraBufferCapacity = 1000)
    val eventFlow: Flow<DevToolsEventDto> = _eventFlow.asSharedFlow()

    val lastState
        get() = dto

    fun addReplica(replica: PhysicalReplica<*>) {
        val dtoReplica = replica.toDto()
        dto.replicas[replica.id.value] = dtoReplica
        _eventFlow.tryEmit(ReplicaCreated(dtoReplica))
        onDtoChanged(dto)
    }

    fun addKeyedReplica(keyedReplica: KeyedPhysicalReplica<*, *>) {
        val dtoReplica = keyedReplica.toDto()
        dto.keyedReplicas[keyedReplica.id.value] = dtoReplica
        _eventFlow.tryEmit(KeyedReplicaCreated(dtoReplica))
        onDtoChanged(dto)
    }

    fun addKeyedReplicaChild(keyedReplicaId: ReplicaId, childReplica: PhysicalReplica<*>) {
        dto.keyedReplicas[keyedReplicaId.value]?.childReplicas?.put(
            childReplica.id.value,
            childReplica.toDto()
        )
        onDtoChanged(dto)
    }

    fun updateReplicaState(replicaId: ReplicaId, state: ReplicaState<*>) {
        val dtoState = state.toDto()
        dto.replicas[replicaId.value]?.state = dtoState
        _eventFlow.tryEmit(ReplicaUpdated(replicaId.value, dtoState))
        onDtoChanged(dto)
    }

    fun updateKeyedReplicaState(keyedReplicaId: ReplicaId, state: KeyedReplicaState) {
        val dtoState = state.toDto()
        dto.keyedReplicas[keyedReplicaId.value]?.state = dtoState
        _eventFlow.tryEmit(KeyedReplicaUpdated(keyedReplicaId.value, dtoState))
        onDtoChanged(dto)
    }

    fun updateKeyedReplicaChildState(
        keyedReplicaId: ReplicaId,
        childReplicaId: ReplicaId,
        state: ReplicaState<*>
    ) {
        val dtoState = state.toDto()
        dto.keyedReplicas[keyedReplicaId.value]
            ?.childReplicas?.get(childReplicaId.value)?.state = dtoState
        _eventFlow.tryEmit(
            KeyedReplicaChildUpdated(
                keyedReplicaId.value,
                childReplicaId.value,
                dtoState
            )
        )
        onDtoChanged(dto)
    }

    fun removeKeyedReplicaChild(keyedReplicaId: ReplicaId, childReplicaId: ReplicaId) {
        dto.keyedReplicas[keyedReplicaId.value]?.childReplicas?.remove(childReplicaId.value)
        onDtoChanged(dto)
    }
}