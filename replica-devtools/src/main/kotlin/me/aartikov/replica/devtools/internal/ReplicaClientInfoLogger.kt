package me.aartikov.replica.devtools.internal

import android.util.Log
import me.aartikov.replica.keyed.KeyedReplicaState
import me.aartikov.replica.single.ReplicaState

class ReplicaClientInfoLogger {

    fun log(clientInfo: ReplicaClientInfo) {
        clientInfo.replicaInfos.values.forEach { info ->
            val details = formatDetails(info.state)
            Log.d("ReplicaDevTools", "${info.name} $details")
        }

        clientInfo.keyedReplicaInfos.values.forEach { info ->
            val details = formatDetails(info.state)
            Log.d("ReplicaDevTools", "${info.name} $details")

            info.childInfos.values.forEach { childInfo ->
                val childDetails = formatDetails(childInfo.state)
                Log.d("ReplicaDevTools", "  ${childInfo.name} $childDetails")
            }
        }
        Log.d("ReplicaDevTools", "____________________________________")
    }

    private fun formatDetails(state: ReplicaState<*>): String {
        var details = ""

        val observingState = state.observingState
        if (observingState.observerCount > 0) details += "o${observingState.observerCount} "
        if (observingState.activeObserverCount > 0) details += "a${observingState.activeObserverCount} "

        val data = state.data
        if (data != null) {
            details += if (data.fresh) "fresh " else "stale "
        }

        if (state.error != null) details += "error "
        if (state.loading) details += "loading "

        return details
    }

    private fun formatDetails(state: KeyedReplicaState): String {
        var details = ""

        if (state.replicaCount > 0) details += "c${state.replicaCount} "
        if (state.replicaWithObserversCount > 0) details += "o${state.replicaWithObserversCount} "
        if (state.replicaWithActiveObserversCount > 0) details += "a${state.replicaWithActiveObserversCount} "

        return details
    }
}