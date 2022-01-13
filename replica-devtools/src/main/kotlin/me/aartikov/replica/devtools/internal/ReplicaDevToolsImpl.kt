package me.aartikov.replica.devtools.internal

import android.util.Log
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.client.ReplicaClientEvent
import me.aartikov.replica.devtools.ReplicaDevTools
import me.aartikov.replica.keyed.KeyedPhysicalReplica
import me.aartikov.replica.keyed.KeyedReplicaEvent
import me.aartikov.replica.keyed.KeyedReplicaId
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaId
import me.aartikov.replica.single.ReplicaState

internal class ReplicaDevToolsImpl(
    private val replicaClient: ReplicaClient
) : ReplicaDevTools {

    private val infos = mutableMapOf<String, ReplicaInfo>()

    override fun launch() {
        replicaClient.eventFlow
            .onEach(::handleReplicaClientEvent)
            .launchIn(replicaClient.coroutineScope)
    }

    private fun launchReplicaProcessing(
        replica: PhysicalReplica<*>,
        keyedReplicaId: KeyedReplicaId?
    ) {
        replica.stateFlow
            .onEach { state ->
                updateInfoDetails(replica.id, keyedReplicaId, state)
                logInfo()
            }
            .launchIn(replica.coroutineScope)
    }

    private fun launchKeyedReplicaProcessing(keyedReplica: KeyedPhysicalReplica<*, *>) {
        keyedReplica.eventFlow
            .onEach { handleKeyedReplicaEvent(keyedReplica.id, it) }
            .launchIn(keyedReplica.coroutineScope)
    }

    private fun handleReplicaClientEvent(event: ReplicaClientEvent) {
        when (event) {
            is ReplicaClientEvent.ReplicaCreated -> {
                infos[event.replica.id.value] = ReplicaInfo(
                    id = event.replica.id.value,
                    name = event.replica.name
                )
                logInfo()
                launchReplicaProcessing(event.replica, keyedReplicaId = null)
            }
            is ReplicaClientEvent.KeyedReplicaCreated -> {
                infos[event.keyedReplica.id.value] = ReplicaInfo(
                    id = event.keyedReplica.id.value,
                    name = event.keyedReplica.name
                )
                logInfo()
                launchKeyedReplicaProcessing(event.keyedReplica)
            }
        }
    }

    private fun handleKeyedReplicaEvent(
        keyedReplicaId: KeyedReplicaId,
        event: KeyedReplicaEvent<*, *>
    ) {
        when (event) {
            is KeyedReplicaEvent.ReplicaCreated -> {
                infos[keyedReplicaId.value]?.childInfos?.let { childInfos ->
                    childInfos[event.replica.id.value] = ReplicaInfo(
                        id = event.replica.id.value,
                        name = event.replica.name
                    )
                }
                logInfo()
                launchReplicaProcessing(event.replica, keyedReplicaId)
            }
            is KeyedReplicaEvent.ReplicaRemoved -> {
                infos[keyedReplicaId.value]?.childInfos?.remove(event.replica.id.value)
                logInfo()
            }
        }
    }

    private fun updateInfoDetails(
        replicaId: ReplicaId,
        keyedReplicaId: KeyedReplicaId?,
        state: ReplicaState<*>
    ) {
        val info = if (keyedReplicaId == null) {
            infos[replicaId.value]
        } else {
            infos[keyedReplicaId.value]?.childInfos?.get(replicaId.value)
        } ?: return

        var details = ""
        if (state.observerCount > 0) details += "o${state.observerCount} "
        if (state.activeObserverCount > 0) details += "a${state.activeObserverCount} "
        if (state.data != null) details += "d "
        if (state.error != null) details += "e "
        if (state.loading) details += "L "
        info.details = details
    }

    private fun logInfo() {
        infos.forEach { (_, info) ->
            val childCount = info.childInfos.size
            if (childCount > 0) {
                Log.d("ReplicaDevTools", "${info.name} ($childCount) ${info.details}")
            } else {
                Log.d("ReplicaDevTools", "${info.name} ${info.details}")
            }

            info.childInfos.forEach { (_, childInfo) ->
                Log.d("ReplicaDevTools", "  ${childInfo.name} ${childInfo.details}")
            }
        }

        Log.d("ReplicaDevTools", "____________________________________")
    }
}