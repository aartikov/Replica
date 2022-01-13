package me.aartikov.replica.devtools.internal

import android.util.Log
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.client.ReplicaClientEvent
import me.aartikov.replica.devtools.ReplicaDevTools
import me.aartikov.replica.keyed.KeyedPhysicalReplica
import me.aartikov.replica.keyed.KeyedReplicaEvent
import me.aartikov.replica.single.PhysicalReplica

internal class ReplicaDevToolsImpl(
    private val replicaClient: ReplicaClient
) : ReplicaDevTools {

    private var replicaCount = 0
    private var keyedReplicaCount = 0

    override fun launch() {
        replicaClient.eventFlow
            .onEach(::handleReplicaClientEvent)
            .launchIn(replicaClient.coroutineScope)
    }

    private fun launchReplicaProcessing(key: Any?, replica: PhysicalReplica<*>) {

    }

    private fun launchKeyedReplicaProcessing(keyedReplica: KeyedPhysicalReplica<*, *>) {
        keyedReplica.eventFlow
            .onEach(::handleKeyedReplicaEvent)
            .launchIn(keyedReplica.coroutineScope)
    }

    private fun handleReplicaClientEvent(event: ReplicaClientEvent) {
        when (event) {
            is ReplicaClientEvent.ReplicaCreated -> {
                replicaCount++
                launchReplicaProcessing(null, event.replica)
            }
            is ReplicaClientEvent.KeyedReplicaCreated -> {
                keyedReplicaCount++
                launchKeyedReplicaProcessing(event.replica)
            }
        }

        Log.d(
            "ReplicaDevTools",
            "replicaCount = $replicaCount, keyedReplicaCount = $keyedReplicaCount"
        )
    }

    private fun handleKeyedReplicaEvent(event: KeyedReplicaEvent<*, *>) {
        when (event) {
            is KeyedReplicaEvent.ReplicaCreated -> {
                replicaCount++
                launchReplicaProcessing(event.key, event.replica)
            }
            is KeyedReplicaEvent.ReplicaRemoved -> {
                replicaCount--
            }
        }

        Log.d(
            "ReplicaDevTools",
            "replicaCount = $replicaCount, keyedReplicaCount = $keyedReplicaCount"
        )
    }
}