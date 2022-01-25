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
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import me.aartikov.replica.common.ReplicaId
import me.aartikov.replica.devtools.dto.*
import java.util.*
import kotlin.collections.LinkedHashSet
import kotlin.coroutines.CoroutineContext

class WebServer(
    coroutineContext: CoroutineContext,
    private val ipAddressProvider: IpAddressProvider,
    private val port: Int,
    private val dtoStore: DtoStore
) {
    private val sessions = Collections.synchronizedSet<WebSocketSession?>(LinkedHashSet())
    private val json = Json

    private val coroutineScope = CoroutineScope(coroutineContext + Dispatchers.IO)

    //TODO Установка порта, остановка сервера
    private val server by lazy {
        embeddedServer(
            factory = Netty,
            port = port,
            host = ipAddressProvider.getLocalIpAddress()
        ) {
            install(WebSockets)
            routing {
                webSocket("/ws") { processNewSession(this) }
                get("/") {
                    call.respondText("Hello to Replica dev tool!")
                }
            }
        }
    }

    fun start() = coroutineScope.launch {
        server.start(true)
    }

    fun sendUpdateReplicaEvent(id: ReplicaId, dto: ReplicaStateDto) {
        broadcast(frame(ReplicaEventDto.serializer(), ReplicaUpdated(id.value, dto)))
    }

    fun sendUpdateKeyedReplicaEvent(id: ReplicaId, dto: KeyedReplicaStateDto) {
        broadcast(frame(ReplicaEventDto.serializer(), KeyedReplicaUpdated(id.value, dto)))
    }

    fun sendReplicaCreatedEvent(dto: ReplicaDto) {
        broadcast(frame(ReplicaEventDto.serializer(), ReplicaCreated(dto)))
    }

    fun sendKeyedReplicaCreatedEvent(dto: KeyedReplicaDto) {
        broadcast(frame(ReplicaEventDto.serializer(), KeyedReplicaCreated(dto)))
    }

    private fun broadcast(frame: Frame) = coroutineScope.launch {
        sessions.forEach { session -> session.send(frame) }
    }

    private fun <T> frame(serializer: SerializationStrategy<T>, value: T): Frame.Text {
        return Frame.Text(json.encodeToString(serializer, value))
    }

    private suspend fun processNewSession(session: WebSocketSession) {
        sessions += session
        val frame = frame(ReplicaEventDto.serializer(), ReplaceAll(dtoStore.lastState))
        session.send(frame)
    }
}

