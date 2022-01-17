package me.aartikov.replica.devtools.internal

import android.util.Log
import me.aartikov.replica.single.ReplicaState

class ReplicaClientInfoLogger {

    fun log(clientInfo: ReplicaClientInfo) {
        clientInfo.replicaInfos.values.forEach { info ->
            val details = formatDetails(info.state)
            Log.d("ReplicaDevTools", "${info.name} $details")
        }

        clientInfo.keyedReplicaInfos.values.forEach { info ->
            val childCount = info.childInfos.size
            if (childCount > 0) {
                Log.d("ReplicaDevTools", "${info.name} ($childCount)")
            } else {
                Log.d("ReplicaDevTools", info.name)
            }

            info.childInfos.values.forEach { childInfo ->
                val details = formatDetails(childInfo.state)
                Log.d("ReplicaDevTools", "  ${childInfo.name} $details")
            }
        }
        Log.d("ReplicaDevTools", "____________________________________")
    }

    private fun formatDetails(state: ReplicaState<*>): String {
        var details = ""

        if (state.observerCount > 0) details += "o${state.observerCount} "
        if (state.activeObserverCount > 0) details += "a${state.activeObserverCount} "
        if (state.data != null) {
            if (state.data!!.fresh) {
                details += "f "
            } else {
                details += "s "
            }
        }
        if (state.error != null) details += "e "
        if (state.loading) details += "L "

        return details
    }
}