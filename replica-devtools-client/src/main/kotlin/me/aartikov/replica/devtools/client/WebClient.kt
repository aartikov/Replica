package me.aartikov.replica.devtools.client

import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import me.aartikov.replica.devtools.dto.*

class WebClient {

    private val client = HttpClient {
        install(WebSockets)
    }

    suspend fun listenSocket(dtoStore: DtoStore) {
        client.webSocket(
            method = HttpMethod.Get,
            host = window.location.hostname,
            port = window.location.port.toIntOrNull() ?: 8080,
            path = "/ws"
        ) {
            for (frame in this.incoming) {
                if (frame is Frame.Text) {

                    val event = Json.decodeFromString(
                        DevToolsEventDto.serializer(),
                        frame.readText()
                    )
                    dtoStore.updateStore(event)
                }
            }
        }
    }

    private fun DtoStore.updateStore(event: DevToolsEventDto) {
        when (event) {
            is ReplaceAll -> updateState(event.replicaClient)
            is ReplicaCreated -> addReplica(event.replica)
            is KeyedReplicaCreated -> addKeyedReplica(event.replica)
            is ReplicaUpdated -> updateReplicaState(event.id, event.state)
            is KeyedReplicaUpdated -> updateKeyedReplicaState(
                event.id,
                event.state
            )
            is KeyedReplicaChildUpdated -> updateKeyedReplicaChildState(
                event.keyedReplicaId,
                event.childReplicaId,
                event.state
            )
        }
    }
}
