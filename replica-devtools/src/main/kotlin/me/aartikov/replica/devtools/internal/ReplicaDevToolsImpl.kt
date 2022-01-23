package me.aartikov.replica.devtools.internal

import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.devtools.ReplicaDevTools

internal class ReplicaDevToolsImpl(
    replicaClient: ReplicaClient
) : ReplicaDevTools {

    private val logger = Logger()
    private val store = DtoStore(
        onDtoChanged = { logger.log(it) }
    )
    private val clientListener = ReplicaClientListener(replicaClient, store)

    override fun launch() {
        clientListener.launch()
    }
}