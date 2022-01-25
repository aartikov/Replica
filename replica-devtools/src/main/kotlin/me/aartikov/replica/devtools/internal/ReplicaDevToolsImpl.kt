package me.aartikov.replica.devtools.internal

import android.content.Context
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.devtools.ReplicaDevTools

internal class ReplicaDevToolsImpl(
    replicaClient: ReplicaClient,
    context: Context
) : ReplicaDevTools {

    private val logger = Logger()
    private val store = DtoStore(
        onDtoChanged = { logger.log(it) }
    )
    private val webServer = WebServer(
        coroutineContext = replicaClient.coroutineScope.coroutineContext,
        ipAddressProvider = IpAddressProvider(context),
        dtoStore = store
    )

    private val clientListener = ReplicaClientListener(
        replicaClient = replicaClient,
        store = store,
        webServer = webServer
    )

    override fun launch() {
        webServer.start()
        clientListener.launch()
    }
}