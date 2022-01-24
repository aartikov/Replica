package me.aartikov.replica.devtools.internal

import android.content.Context
import kotlinx.coroutines.Job
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.devtools.ReplicaDevTools

internal class ReplicaDevToolsImpl(
    replicaClient: ReplicaClient,
    context: Context
) : ReplicaDevTools {

    private val logger = Logger()
    private val store = DtoStore(
        onDtoChanged = {
            logger.log(it)
            webServer.sendEvent(it)
        }
    )
    private val webServer = WebServer(
        coroutineScope = replicaClient.coroutineScope,
        ipAddressProvider = IpAddressProvider(context)
    )

    private var webServerJob: Job? = null
    private val clientListener = ReplicaClientListener(replicaClient, store)

    override fun launch() {
        webServerJob = webServer.start()
        clientListener.launch()
    }
}