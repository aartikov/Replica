package me.aartikov.replica.devtools

import android.content.Context
import me.aartikov.replica.client.ReplicaClient

interface ReplicaDevTools {

    fun launch()
}

@Suppress("UNUSED_PARAMETER")
fun ReplicaDevTools(
    replicaClient: ReplicaClient,
    applicationContext: Context,
    settings: DevToolsSettings = DevToolsSettings()
): ReplicaDevTools {
    return object : ReplicaDevTools {
        override fun launch() {
            // nothing
        }
    }
}