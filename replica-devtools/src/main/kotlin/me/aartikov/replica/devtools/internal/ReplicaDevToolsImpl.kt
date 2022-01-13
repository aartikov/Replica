package me.aartikov.replica.devtools.internal

import android.util.Log
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.client.ReplicaClientEvent
import me.aartikov.replica.devtools.ReplicaDevTools
import me.aartikov.replica.keyed.KeyedPhysicalReplica
import me.aartikov.replica.single.PhysicalReplica

internal class ReplicaDevToolsImpl(
    private val replicaClient: ReplicaClient
) : ReplicaDevTools {
    private val coroutineScope = replicaClient.coroutineScope

    private var replicaCount = 0
    private var keyedReplicaCount = 0

    override fun launch() {
        replicaClient.eventFlow
            .onEach(::handleReplicaClientEvent)
            .launchIn(coroutineScope)
    }

    private fun handleReplicaClientEvent(event: ReplicaClientEvent) {
        when (event) {
            is ReplicaClientEvent.ReplicaCreated -> {
                replicaCount++
                launchReplicaProcessing(event.replica)
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

    private fun launchReplicaProcessing(replica: PhysicalReplica<*>) {

    }

    private fun launchKeyedReplicaProcessing(keyedReplica: KeyedPhysicalReplica<*, *>) {

    }
}