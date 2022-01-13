package me.aartikov.replica.devtools

import me.aartikov.replica.client.ReplicaClient

interface ReplicaDevTools {

    fun launch()
}

@Suppress("UNUSED_PARAMETER")
fun ReplicaDevTools(replicaClient: ReplicaClient): ReplicaDevTools {
    return object : ReplicaDevTools {
        override fun launch() {
            // nothing
        }
    }
}