package me.aartikov.replica.devtools.internal

import me.aartikov.replica.common.ReplicaId
import me.aartikov.replica.keyed.KeyedPhysicalReplica
import me.aartikov.replica.keyed.KeyedReplicaState
import me.aartikov.replica.keyed.currentState
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaState
import me.aartikov.replica.single.currentState

class ReplicaClientInfoStore(
    val onInfoChanged: (ReplicaClientInfo) -> Unit
) {

    private val info = ReplicaClientInfo()

    fun addReplica(replica: PhysicalReplica<*>) {
        info.replicaInfos[replica.id] = ReplicaInfo(
            id = replica.id,
            name = replica.name,
            settings = replica.settings,
            state = replica.currentState
        )
        onInfoChanged(info)
    }

    fun addKeyedReplica(keyedReplica: KeyedPhysicalReplica<*, *>) {
        info.keyedReplicaInfos[keyedReplica.id] = KeyedReplicaInfo(
            id = keyedReplica.id,
            name = keyedReplica.name,
            state = keyedReplica.currentState
        )
        onInfoChanged(info)
    }

    fun addKeyedReplicaChild(replica: PhysicalReplica<*>, keyedReplicaId: ReplicaId) {
        info.keyedReplicaInfos[keyedReplicaId]?.childInfos?.put(
            replica.id,
            ReplicaInfo(
                id = replica.id,
                name = replica.name,
                settings = replica.settings,
                state = replica.currentState
            )
        )
        onInfoChanged(info)
    }

    fun updateReplicaState(id: ReplicaId, state: ReplicaState<*>) {
        info.replicaInfos[id]?.state = state
        onInfoChanged(info)
    }

    fun updateKeyedReplicaState(id: ReplicaId, state: KeyedReplicaState) {
        info.keyedReplicaInfos[id]?.state = state
        onInfoChanged(info)
    }

    fun updateKeyedReplicaChildState(
        childId: ReplicaId,
        parentId: ReplicaId,
        state: ReplicaState<*>
    ) {
        info.keyedReplicaInfos[parentId]?.childInfos?.get(childId)?.state = state
        onInfoChanged(info)
    }

    fun removeKeyedReplicaChild(childId: ReplicaId, parentId: ReplicaId) {
        info.keyedReplicaInfos[parentId]?.childInfos?.remove(childId)
        onInfoChanged(info)
    }
}