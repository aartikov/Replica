package me.aartikov.replica.devtools.internal

import kotlinx.coroutines.Job
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.devtools.ReplicaDevTools

internal class ReplicaDevToolsImpl(
    replicaClient: ReplicaClient
) : ReplicaDevTools {

    private val logger = Logger()
    private val store = DtoStore(
        onDtoChanged = { logger.log(it) }
    )
    private val webServer = WebServer(replicaClient.coroutineScope)
    private var webServerJob: Job? = null
    private val clientListener = ReplicaClientListener(replicaClient, store)

    override fun launch() {
        webServerJob = webServer.start()
        clientListener.launch()
    }
}