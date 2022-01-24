package me.aartikov.replica.devtools.internal

import io.ktor.application.call
import io.ktor.application.install
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.aartikov.replica.devtools.dto.ReplicaClientDto
import java.util.*
import kotlin.collections.LinkedHashSet

internal class WebServer(
    private val coroutineScope: CoroutineScope,
    private val ipAddressProvider: IpAddressProvider
) {

    companion object {
        private const val TAG = "Replica WebServer"
    }

    private val sessions = Collections.synchronizedSet<WebSocketSession?>(LinkedHashSet())

    //TODO Установка порта
    private val server by lazy {
        embeddedServer(Netty, port = 8080, host = ipAddressProvider.getLocalIpAddress()) {
            install(WebSockets) {
            }
            routing {
                webSocket("/ws") {
                    sessions += this
                    this.send(Frame.Text("Hello"))
                }
                get("/") {
                    call.respondText("Hello to Replica dev tool!")
                }
            }
        }
    }

    fun start() = coroutineScope.launch(Dispatchers.IO) {
        server.start(true)
    }

    fun sendEvent(dto: ReplicaClientDto) = coroutineScope.launch(Dispatchers.IO) {
        sessions.forEach { session ->
            session.send(Frame.Text(Json.encodeToString(dto)))
        }
    }
}