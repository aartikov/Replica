package me.aartikov.replica.devtools.internal

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.client.ReplicaClientEvent
import me.aartikov.replica.common.ReplicaId
import me.aartikov.replica.devtools.dto.DtoStore
import me.aartikov.replica.keyed.KeyedPhysicalReplica
import me.aartikov.replica.keyed.KeyedReplicaEvent
import me.aartikov.replica.keyed_paged.KeyedPagedPhysicalReplica
import me.aartikov.replica.keyed_paged.KeyedPagedReplicaEvent
import me.aartikov.replica.paged.PagedPhysicalReplica
import me.aartikov.replica.single.PhysicalReplica

internal class ReplicaClientListener(
    private val replicaClient: ReplicaClient,
    private val store: DtoStore
) {

    fun launch() {
        replicaClient.eventFlow
            .onEach(::handleReplicaClientEvent)
            .launchIn(replicaClient.coroutineScope)
    }

    private fun handleReplicaClientEvent(event: ReplicaClientEvent) {
        when (event) {
            is ReplicaClientEvent.ReplicaCreated -> {
                store.addReplica(event.replica.toDto())
                launchReplicaProcessing(event.replica)
            }

            is ReplicaClientEvent.KeyedReplicaCreated -> {
                store.addKeyedReplica(event.keyedReplica.toDto())
                launchKeyedReplicaProcessing(event.keyedReplica)
            }

            is ReplicaClientEvent.PagedReplicaCreated -> {
                store.addReplica(event.replica.toDto())
                launchPagedReplicaProcessing(event.replica)
            }

            is ReplicaClientEvent.KeyedPagedReplicaCreated -> {
                store.addKeyedReplica(event.keyedPagedReplica.toDto())
                launchKeyedPagedReplicaProcessing(event.keyedPagedReplica)
            }
        }
    }

    private fun launchReplicaProcessing(replica: PhysicalReplica<*>) {
        replica.stateFlow
            .onEach { state ->
                store.updateReplicaState(replica.id.value, state.toDto())
            }
            .launchIn(replica.coroutineScope)
    }

    private fun launchPagedReplicaProcessing(replica: PagedPhysicalReplica<*, *>) {
        replica.stateFlow
            .onEach { state ->
                store.updateReplicaState(replica.id.value, state.toDto())
            }
            .launchIn(replica.coroutineScope)
    }

    private fun launchKeyedReplicaProcessing(keyedReplica: KeyedPhysicalReplica<*, *>) {
        keyedReplica.stateFlow
            .onEach { state ->
                store.updateKeyedReplicaState(keyedReplica.id.value, state.toDto())
            }
            .launchIn(keyedReplica.coroutineScope)

        keyedReplica.eventFlow
            .onEach { handleKeyedReplicaEvent(keyedReplica.id, it) }
            .launchIn(keyedReplica.coroutineScope)
    }

    private fun launchKeyedPagedReplicaProcessing(keyedReplica: KeyedPagedPhysicalReplica<*, *, *>) {
        keyedReplica.stateFlow
            .onEach { state ->
                store.updateKeyedReplicaState(keyedReplica.id.value, state.toDto())
            }
            .launchIn(keyedReplica.coroutineScope)

        keyedReplica.eventFlow
            .onEach {
                handleKeyedPagedReplicaEvent(keyedReplica.id, it)
            }
            .launchIn(keyedReplica.coroutineScope)
    }

    private fun handleKeyedPagedReplicaEvent(
        keyedReplicaId: ReplicaId,
        event: KeyedPagedReplicaEvent<*, *, *>
    ) {
        when (event) {
            is KeyedPagedReplicaEvent.ReplicaCreated -> {
                store.addKeyedReplicaChild(keyedReplicaId.value, event.replica.toDto())
                launchKeyedPagedReplicaChildProcessing(keyedReplicaId, event.replica)
            }

            is KeyedPagedReplicaEvent.ReplicaRemoved -> {
                store.removeKeyedReplicaChild(keyedReplicaId.value, event.replicaId.value)
            }
        }
    }

    private fun handleKeyedReplicaEvent(
        keyedReplicaId: ReplicaId,
        event: KeyedReplicaEvent<*, *>
    ) {
        when (event) {
            is KeyedReplicaEvent.ReplicaCreated -> {
                store.addKeyedReplicaChild(keyedReplicaId.value, event.replica.toDto())
                launchKeyedReplicaChildProcessing(keyedReplicaId, event.replica)
            }

            is KeyedReplicaEvent.ReplicaRemoved -> {
                store.removeKeyedReplicaChild(keyedReplicaId.value, event.replicaId.value)
            }
        }
    }

    private fun launchKeyedReplicaChildProcessing(
        keyedReplicaId: ReplicaId,
        childReplica: PhysicalReplica<*>
    ) {
        childReplica.stateFlow
            .onEach { state ->
                store.updateKeyedReplicaChildState(
                    keyedReplicaId.value,
                    childReplica.id.value,
                    state.toDto()
                )
            }
            .launchIn(childReplica.coroutineScope)
    }

    private fun launchKeyedPagedReplicaChildProcessing(
        keyedReplicaId: ReplicaId,
        childReplica: PagedPhysicalReplica<*, *>
    ) {
        childReplica.stateFlow
            .onEach { state ->
                store.updateKeyedReplicaChildState(
                    keyedReplicaId.value,
                    childReplica.id.value,
                    state.toDto()
                )
            }
            .launchIn(childReplica.coroutineScope)
    }
}