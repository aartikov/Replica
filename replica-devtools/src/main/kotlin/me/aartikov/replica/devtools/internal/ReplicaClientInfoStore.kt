package me.aartikov.replica.devtools.internal

import me.aartikov.replica.keyed.KeyedPhysicalReplica
import me.aartikov.replica.keyed.KeyedReplicaId
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaId
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
            name = keyedReplica.name
        )
        onInfoChanged(info)
    }

    fun addChildReplica(replica: PhysicalReplica<*>, parentId: KeyedReplicaId) {
        info.keyedReplicaInfos[parentId]?.childInfos?.put(
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

    fun updateChildReplicaState(id: ReplicaId, parentId: KeyedReplicaId, state: ReplicaState<*>) {
        info.keyedReplicaInfos[parentId]?.childInfos?.get(id)?.state = state
        onInfoChanged(info)
    }

    fun removeChildReplica(id: ReplicaId, parentId: KeyedReplicaId) {
        info.keyedReplicaInfos[parentId]?.childInfos?.remove(id)
        onInfoChanged(info)
    }
}