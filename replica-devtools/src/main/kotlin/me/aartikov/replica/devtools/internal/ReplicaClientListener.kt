package me.aartikov.replica.devtools.internal

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.client.ReplicaClientEvent
import me.aartikov.replica.common.ReplicaId
import me.aartikov.replica.keyed.KeyedPhysicalReplica
import me.aartikov.replica.keyed.KeyedReplicaEvent
import me.aartikov.replica.single.PhysicalReplica

class ReplicaClientListener(
    private val replicaClient: ReplicaClient,
    private val store: DtoStore,
    private val webServer: WebServer
) {

    fun launch() {
        replicaClient.eventFlow
            .onEach(::handleReplicaClientEvent)
            .launchIn(replicaClient.coroutineScope)
    }

    private fun handleReplicaClientEvent(event: ReplicaClientEvent) {
        when (event) {
            is ReplicaClientEvent.ReplicaCreated -> {
                store.addReplica(event.replica)
                webServer.sendReplicaCreatedEvent(event.replica.toDto())
                launchReplicaProcessing(event.replica)
            }
            is ReplicaClientEvent.KeyedReplicaCreated -> {
                store.addKeyedReplica(event.keyedReplica)
                webServer.sendKeyedReplicaCreatedEvent(event.keyedReplica.toDto())
                launchKeyedReplicaProcessing(event.keyedReplica)
            }
        }
    }

    private fun launchReplicaProcessing(replica: PhysicalReplica<*>) {
        replica.stateFlow
            .onEach { state ->
                store.updateReplicaState(replica.id, state)
                webServer.sendUpdateReplicaEvent(replica.id, state.toDto())
            }
            .launchIn(replica.coroutineScope)
    }

    private fun launchKeyedReplicaProcessing(keyedReplica: KeyedPhysicalReplica<*, *>) {
        keyedReplica.stateFlow
            .onEach { state ->
                store.updateKeyedReplicaState(keyedReplica.id, state)
                webServer.sendUpdateKeyedReplicaEvent(keyedReplica.id, state.toDto())
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
                store.addKeyedReplicaChild(keyedReplicaId, event.replica)
                launchKeyedReplicaChildProcessing(keyedReplicaId, event.replica)
            }
            is KeyedReplicaEvent.ReplicaRemoved -> {
                store.removeKeyedReplicaChild(keyedReplicaId, event.replicaId)
            }
        }
    }

    private fun launchKeyedReplicaChildProcessing(
        keyedReplicaId: ReplicaId,
        childReplica: PhysicalReplica<*>
    ) {
        childReplica.stateFlow
            .onEach { state ->
                store.updateKeyedReplicaChildState(keyedReplicaId, childReplica.id, state)
            }
            .launchIn(childReplica.coroutineScope)
    }
}