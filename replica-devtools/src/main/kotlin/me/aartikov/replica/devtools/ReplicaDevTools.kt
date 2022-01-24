package me.aartikov.replica.devtools

import android.content.Context
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.devtools.internal.ReplicaDevToolsImpl

interface ReplicaDevTools {

    fun launch()
}

fun ReplicaDevTools(replicaClient: ReplicaClient, context: Context): ReplicaDevTools {
    return ReplicaDevToolsImpl(replicaClient, context)
}