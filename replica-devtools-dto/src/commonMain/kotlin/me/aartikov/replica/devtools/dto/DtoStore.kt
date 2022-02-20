package me.aartikov.replica.devtools.dto

import kotlinx.coroutines.flow.*

class DtoStore {

    private val _dtoFlow = MutableStateFlow(ReplicaClientDto())
    val dtoFlow: StateFlow<ReplicaClientDto> = _dtoFlow.asStateFlow()

    private val _eventFlow = MutableSharedFlow<DevToolsEventDto>(extraBufferCapacity = 1000)
    val eventFlow: Flow<DevToolsEventDto> = _eventFlow.asSharedFlow()

    fun addReplica(replica: ReplicaDto) {
        _dtoFlow.update {
            it.copy(replicas = it.replicas.plus(replica.id to replica))
        }
        _eventFlow.tryEmit(ReplicaCreated(replica))
    }

    fun addKeyedReplica(keyedReplica: KeyedReplicaDto) {
        _dtoFlow.update {
            it.copy(keyedReplicas = it.keyedReplicas.plus(keyedReplica.id to keyedReplica))
        }
        _eventFlow.tryEmit(KeyedReplicaCreated(keyedReplica))
    }

    fun addKeyedReplicaChild(keyedReplicaId: String, childReplica: ReplicaDto) {
        _dtoFlow.update {
            val keyedReplica = it.keyedReplicas[keyedReplicaId] ?: return
            val updatedKeyedReplica = keyedReplica.copy(
                childReplicas = keyedReplica.childReplicas.plus(childReplica.id to childReplica)
            )
            it.copy(
                keyedReplicas = it.keyedReplicas.plus(keyedReplicaId to updatedKeyedReplica)
            )
        }
        _eventFlow.tryEmit(KeyedReplicaChildCreated(keyedReplicaId, childReplica))
    }

    fun updateReplicaState(replicaId: String, state: ReplicaStateDto) {
        _dtoFlow.update {
            val updatedReplica = it.replicas[replicaId]?.copy(state = state) ?: return
            it.copy(
                replicas = it.replicas.plus(replicaId to updatedReplica)
            )
        }
        _eventFlow.tryEmit(ReplicaUpdated(replicaId, state))
    }

    fun updateKeyedReplicaState(keyedReplicaId: String, state: KeyedReplicaStateDto) {
        _dtoFlow.update {
            val updatedReplica = it.keyedReplicas[keyedReplicaId]?.copy(state = state) ?: return
            it.copy(
                keyedReplicas = it.keyedReplicas.plus(keyedReplicaId to updatedReplica)
            )
        }
        _eventFlow.tryEmit(KeyedReplicaUpdated(keyedReplicaId, state))
    }

    fun updateKeyedReplicaChildState(
        keyedReplicaId: String,
        childReplicaId: String,
        state: ReplicaStateDto
    ) {
        _dtoFlow.update {
            val keyedReplica = it.keyedReplicas[keyedReplicaId] ?: return
            val updatedChildReplica = keyedReplica.childReplicas[childReplicaId]
                ?.copy(state = state)
                ?: return
            val updatedKeyedReplica = keyedReplica.copy(
                childReplicas = keyedReplica.childReplicas.plus(
                    childReplicaId to updatedChildReplica
                )
            )
            it.copy(keyedReplicas = it.keyedReplicas.plus(keyedReplicaId to updatedKeyedReplica))
        }
        _eventFlow.tryEmit(
            KeyedReplicaChildUpdated(
                keyedReplicaId,
                childReplicaId,
                state
            )
        )
    }

    fun removeKeyedReplicaChild(keyedReplicaId: String, childReplicaId: String) {
        _dtoFlow.update {
            val keyedReplica = it.keyedReplicas[keyedReplicaId] ?: return
            val updatedKeyedReplica = keyedReplica.copy(
                childReplicas = keyedReplica.childReplicas.minus(childReplicaId)
            )
            it.copy(
                keyedReplicas = it.keyedReplicas.plus(keyedReplicaId to updatedKeyedReplica)
            )
        }
        _eventFlow.tryEmit(KeyedReplicaChildRemoved(keyedReplicaId, childReplicaId))
    }

    fun updateState(state: ReplicaClientDto) {
        _dtoFlow.update { state }
    }
}