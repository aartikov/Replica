package me.aartikov.replica.devtools.internal

import android.util.Log
import io.ktor.application.install
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import me.aartikov.replica.devtools.dto.DevToolsEventDto
import me.aartikov.replica.devtools.dto.DtoStore
import me.aartikov.replica.devtools.dto.ReplaceAll

class WebServer(
    private val coroutineScope: CoroutineScope,
    ipAddressProvider: IpAddressProvider,
    private val port: Int,
    private val dtoStore: DtoStore
) {
    private val json = Json
    private val ipAddress = ipAddressProvider.getLocalIpAddress()

    private val server by lazy {
        embeddedServer(
            factory = Netty,
            port = port,
            host = ipAddress
        ) {
            install(WebSockets)
            routing {
                webSocket("/ws") { processSession(this) }
                static("/") {
                    resources()
                }
            }

        }
    }

    private suspend fun processSession(session: WebSocketSession) {
        val frame = frame(DevToolsEventDto.serializer(), ReplaceAll(dtoStore.lastState))
        session.send(frame)
        dtoStore.eventFlow.collect { event ->
            session.send(frame(DevToolsEventDto.serializer(), event))
        }
    }

    fun launch() = coroutineScope.launch(Dispatchers.IO) {
        Log.d(
            "ReplicaServer",
            "Devtool is available with address: http://$ipAddress:$port/index.html"
        )
        server.start(true)
    }

    private fun <T> frame(serializer: SerializationStrategy<T>, value: T): Frame.Text {
        return Frame.Text(json.encodeToString(serializer, value))
    }
}