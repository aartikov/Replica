package me.aartikov.replica.devtools.client

import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.WebSocketException
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import me.aartikov.replica.devtools.dto.*

class WebClient {

    private val mutableConnectionStatus = MutableStateFlow<ConnectionStatus>(
        ConnectionStatus.Attempt
    )
    val connectionStatus: StateFlow<ConnectionStatus>
        get() = mutableConnectionStatus

    private val client = HttpClient {
        install(WebSockets)
    }

    suspend fun startListenSocket(dtoStore: DtoStore) {
        listenSocket(dtoStore)
    }

    private suspend fun listenSocket(dtoStore: DtoStore) {
        try {
            mutableConnectionStatus.emit(ConnectionStatus.Attempt)
            connectToSocket(dtoStore)
        } catch (e: WebSocketException) {
            mutableConnectionStatus.emit(ConnectionStatus.Failed)
            delay(3000L)
            listenSocket(dtoStore)
        }
    }

    private suspend fun connectToSocket(dtoStore: DtoStore) {
        client.webSocket(
            method = HttpMethod.Get,
            host = "10.0.1.207",
            port = 8080,
            path = "/ws"
        ) {
            try {
                mutableConnectionStatus.emit(ConnectionStatus.Connected)
                while (true) {
                    val frame = incoming.receive()
                    if (frame is Frame.Text) {
                        val event = Json.decodeFromString(
                            DevToolsEventDto.serializer(),
                            frame.readText()
                        )
                        dtoStore.updateStore(event)
                    }
                }
            } catch (e: ClosedReceiveChannelException) {
                mutableConnectionStatus.emit(ConnectionStatus.Failed)
                listenSocket(dtoStore)
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
            is KeyedReplicaChildRemoved -> removeKeyedReplicaChild(
                keyedReplicaId = event.keyedReplicaId,
                childReplicaId = event.childReplicaId
            )
            is KeyedReplicaChildCreated -> addKeyedReplicaChild(
                event.keyedReplicaId,
                event.childReplica
            )
        }
    }
}

sealed interface ConnectionStatus {
    object Connected : ConnectionStatus
    object Failed : ConnectionStatus
    object Attempt : ConnectionStatus
}