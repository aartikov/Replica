package me.aartikov.replica.devtools.internal

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.client.ReplicaClientEvent
import me.aartikov.replica.common.ReplicaId
import me.aartikov.replica.devtools.ReplicaDevTools
import me.aartikov.replica.keyed.KeyedPhysicalReplica
import me.aartikov.replica.keyed.KeyedReplicaEvent
import me.aartikov.replica.single.PhysicalReplica

internal class ReplicaDevToolsImpl(
    private val replicaClient: ReplicaClient
) : ReplicaDevTools {

    private val logger = ReplicaClientInfoLogger()
    private val store = ReplicaClientInfoStore(
        onInfoChanged = { logger.log(it) }
    )

    override fun launch() {
        replicaClient.eventFlow
            .onEach(::handleReplicaClientEvent)
            .launchIn(replicaClient.coroutineScope)
    }

    private fun handleReplicaClientEvent(event: ReplicaClientEvent) {
        when (event) {
            is ReplicaClientEvent.ReplicaCreated -> {
                store.addReplica(event.replica)
                launchReplicaProcessing(event.replica)
            }
            is ReplicaClientEvent.KeyedReplicaCreated -> {
                store.addKeyedReplica(event.keyedReplica)
                launchKeyedReplicaProcessing(event.keyedReplica)
            }
        }
    }

    private fun launchReplicaProcessing(replica: PhysicalReplica<*>) {
        replica.stateFlow
            .onEach { state ->
                store.updateReplicaState(replica.id, state)
            }
            .launchIn(replica.coroutineScope)
    }

    private fun launchKeyedReplicaProcessing(keyedReplica: KeyedPhysicalReplica<*, *>) {
        keyedReplica.stateFlow
            .onEach { state ->
                store.updateKeyedReplicaState(keyedReplica.id, state)
            }
            .launchIn(keyedReplica.coroutineScope)

        keyedReplica.eventFlow
            .onEach { handleKeyedReplicaEvent(keyedReplica.id, it) }
            .launchIn(keyedReplica.coroutineScope)
    }

    private fun handleKeyedReplicaEvent(
        keyedReplicaId: ReplicaId,
        event: KeyedReplicaEvent<*, *>
    ) {
        when (event) {
            is KeyedReplicaEvent.ReplicaCreated -> {
                store.addKeyedReplicaChild(event.replica, keyedReplicaId)
                launchKeyedReplicaChildProcessing(event.replica, keyedReplicaId)
            }
            is KeyedReplicaEvent.ReplicaRemoved -> {
                store.removeKeyedReplicaChild(event.replicaId, keyedReplicaId)
            }
        }
    }

    private fun launchKeyedReplicaChildProcessing(
        replica: PhysicalReplica<*>,
        parentId: ReplicaId
    ) {
        replica.stateFlow
            .onEach { state ->
                store.updateKeyedReplicaChildState(replica.id, parentId, state)
            }
            .launchIn(replica.coroutineScope)
    }
}