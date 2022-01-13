package me.aartikov.replica.devtools

import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.devtools.internal.ReplicaDevToolsImpl

interface ReplicaDevTools {

    fun launch()
}

fun ReplicaDevTools(replicaClient: ReplicaClient): ReplicaDevTools {
    return ReplicaDevToolsImpl(replicaClient)
}