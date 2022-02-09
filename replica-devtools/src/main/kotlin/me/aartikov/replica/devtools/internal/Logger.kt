package me.aartikov.replica.devtools.internal

import android.util.Log
import me.aartikov.replica.devtools.dto.KeyedReplicaStateDto
import me.aartikov.replica.devtools.dto.ReplicaClientDto
import me.aartikov.replica.devtools.dto.ReplicaStateDto

class Logger {

    fun log(client: ReplicaClientDto) {
        client.replicas.values.forEach { replica ->
            val details = formatDetails(replica.state)
            Log.d("ReplicaDevTools", "${replica.name} $details")
        }

        client.keyedReplicas.values.forEach { keyedReplica ->
            val details = formatDetails(keyedReplica.state)
            Log.d("ReplicaDevTools", "${keyedReplica.name} $details")

            keyedReplica.childReplicas.values.forEach { childReplica ->
                val childDetails = formatDetails(childReplica.state)
                Log.d("ReplicaDevTools", "  ${childReplica.name} $childDetails")
            }
        }
        Log.d("ReplicaDevTools", "____________________________________")
    }

    private fun formatDetails(state: ReplicaStateDto): String {
        var details = ""

        if (state.observerCount > 0) details += "o${state.observerCount} "
        if (state.activeObserverCount > 0) details += "a${state.activeObserverCount} "

        if (state.hasData) {
            details += if (state.dataIsFresh) "fresh " else "stale "
        }

        if (state.hasError) details += "error "
        if (state.loading) details += "loading "

        return details
    }

    private fun formatDetails(state: KeyedReplicaStateDto): String {
        var details = ""

        if (state.replicaCount > 0) details += "c${state.replicaCount} "
        if (state.replicaWithObserversCount > 0) details += "o${state.replicaWithObserversCount} "
        if (state.replicaWithActiveObserversCount > 0) details += "a${state.replicaWithActiveObserversCount} "

        return details
    }
}