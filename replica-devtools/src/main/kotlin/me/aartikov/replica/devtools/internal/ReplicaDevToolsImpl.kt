package me.aartikov.replica.devtools.internal

import android.content.Context
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.devtools.DevToolsSettings
import me.aartikov.replica.devtools.ReplicaDevTools
import me.aartikov.replica.devtools.dto.DtoStore

internal class ReplicaDevToolsImpl(
    private val replicaClient: ReplicaClient,
    settings: DevToolsSettings,
    context: Context
) : ReplicaDevTools {

    private val logger = Logger()
    private val store = DtoStore()

    private val webServer = WebServer(
        coroutineScope = replicaClient.coroutineScope,
        ipAddressProvider = IpAddressProvider(context),
        port = settings.port,
        dtoStore = store
    )

    private val clientListener = ReplicaClientListener(
        replicaClient = replicaClient,
        store = store
    )

    override fun launch() {
        clientListener.launch()
        webServer.launch()
        launchLogger()
    }

    private fun launchLogger() {
        replicaClient.coroutineScope.launch {
            store.stateDto.collect { logger.log(it) }
        }
    }
}