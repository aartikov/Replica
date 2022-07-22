package me.aartikov.replica.devtools.client

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.browser.window
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import me.aartikov.replica.devtools.dto.*

class WebClient {

    private val _connectionStatusFlow = MutableStateFlow<ConnectionStatus>(
        ConnectionStatus.Attempt
    )
    val connectionStatusFlow: StateFlow<ConnectionStatus> = _connectionStatusFlow.asStateFlow()

    private val client = HttpClient {
        install(WebSockets)
    }

    suspend fun startListenSocket(dtoStore: DtoStore) {
        listenSocket(dtoStore)
    }

    private suspend fun listenSocket(dtoStore: DtoStore) {
        try {
            _connectionStatusFlow.emit(ConnectionStatus.Attempt)
            connectToSocket(dtoStore)
        } catch (e: WebSocketException) {
            _connectionStatusFlow.emit(ConnectionStatus.Failed)
            delay(3000L)
            listenSocket(dtoStore)
        }
    }

    private suspend fun connectToSocket(dtoStore: DtoStore) {
        client.webSocket(
            method = HttpMethod.Get,
            host = window.location.hostname,
            port = window.location.port.toIntOrNull() ?: 8080,
            path = "/ws"
        ) {
            try {
                _connectionStatusFlow.emit(ConnectionStatus.Connected)
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
                _connectionStatusFlow.emit(ConnectionStatus.Failed)
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