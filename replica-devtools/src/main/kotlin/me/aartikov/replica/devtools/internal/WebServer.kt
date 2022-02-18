package me.aartikov.replica.devtools.internal

import android.util.Log
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import io.netty.util.internal.logging.InternalLoggerFactory
import io.netty.util.internal.logging.JdkLoggerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import me.aartikov.replica.devtools.dto.DevToolsEventDto
import me.aartikov.replica.devtools.dto.DtoStore
import me.aartikov.replica.devtools.dto.ReplaceAll

internal class WebServer(
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
                    resources("replica-devtools")
                }
            }

        }
    }

    private suspend fun processSession(session: WebSocketSession) {
        val lastState = dtoStore.dtoFlow.firstOrNull()
        lastState?.let {
            val frame = frame(DevToolsEventDto.serializer(), ReplaceAll(lastState))
            session.send(frame)
        }
        dtoStore.eventFlow.collect { event ->
            session.send(frame(DevToolsEventDto.serializer(), event))
        }
    }

    fun launch() = coroutineScope.launch(Dispatchers.IO) {
        Log.d(
            "ReplicaDevTools",
            "Devtool is available with address: http://$ipAddress:$port/index.html"
        )
        InternalLoggerFactory.setDefaultFactory(JdkLoggerFactory.INSTANCE)
        server.start(true)
    }

    private fun <T> frame(serializer: SerializationStrategy<T>, value: T): Frame.Text {
        return Frame.Text(json.encodeToString(serializer, value))
    }
}