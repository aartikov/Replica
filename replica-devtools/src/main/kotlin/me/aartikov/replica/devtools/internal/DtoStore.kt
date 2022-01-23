package me.aartikov.replica.devtools.internal

import me.aartikov.replica.common.ReplicaId
import me.aartikov.replica.devtools.dto.ReplicaClientDto
import me.aartikov.replica.keyed.KeyedPhysicalReplica
import me.aartikov.replica.keyed.KeyedReplicaState
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaState

class DtoStore(
    val onDtoChanged: (ReplicaClientDto) -> Unit
) {

    private val dto = ReplicaClientDto()

    fun addReplica(replica: PhysicalReplica<*>) {
        dto.replicas[replica.id.value] = replica.toDto()
        onDtoChanged(dto)
    }

    fun addKeyedReplica(keyedReplica: KeyedPhysicalReplica<*, *>) {
        dto.keyedReplicas[keyedReplica.id.value] = keyedReplica.toDto()
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
        dto.replicas[replicaId.value]?.state = state.toDto()
        onDtoChanged(dto)
    }

    fun updateKeyedReplicaState(keyedReplicaId: ReplicaId, state: KeyedReplicaState) {
        dto.keyedReplicas[keyedReplicaId.value]?.state = state.toDto()
        onDtoChanged(dto)
    }

    fun updateKeyedReplicaChildState(
        keyedReplicaId: ReplicaId,
        childReplicaId: ReplicaId,
        state: ReplicaState<*>
    ) {
        dto.keyedReplicas[keyedReplicaId.value]
            ?.childReplicas?.get(childReplicaId.value)?.state = state.toDto()
        onDtoChanged(dto)
    }

    fun removeKeyedReplicaChild(keyedReplicaId: ReplicaId, childReplicaId: ReplicaId) {
        dto.keyedReplicas[keyedReplicaId.value]?.childReplicas?.remove(childReplicaId.value)
        onDtoChanged(dto)
    }
}