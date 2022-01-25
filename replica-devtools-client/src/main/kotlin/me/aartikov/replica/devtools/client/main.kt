package me.aartikov.replica.devtools.client

import androidx.compose.runtime.*
import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import kotlinx.browser.document
import kotlinx.serialization.json.Json
import me.aartikov.replica.devtools.client.components.Card
import me.aartikov.replica.devtools.client.components.Crossfade
import me.aartikov.replica.devtools.client.components.NavBar
import me.aartikov.replica.devtools.dto.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.keywords.auto
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Ul
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement

private val client = HttpClient {
    install(WebSockets)
}

fun main() {
    val rootElement = document.getElementById("root") as HTMLElement

    renderComposable(root = rootElement) {
        var state by remember { mutableStateOf(ReplicaClientDto()) }
        val dtoStore = DtoStore { state = it }

        LaunchedEffect(state) { listenSocket(dtoStore) }
        Body(state)
    }
}

// TODO() Установка ip, порта
private suspend fun listenSocket(dtoStore: DtoStore) {
    client.webSocket(
        method = HttpMethod.Get,
        host = "192.168.0.8",
        port = 8080,
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

@Composable
private fun Body(state: ReplicaClientDto) {
    Card(
        attrs = {
            style {
                position(Position.Absolute)
                width(90.percent)
                height(100.percent)
                top(0.px)
                bottom(0.px)
                left(0.px)
                right(0.px)
                property("margin", auto)
            }
        }
    ) {
        Crossfade(
            target = state,
            attrs = {
                style {
                    width(100.percent)
                    height(100.percent)
                    position(Position.Relative)
                    left(0.px)
                    top(0.px)
                }
            }
        ) { state -> Content(state) }
    }
}

@Composable
fun Content(replicaClientDto: ReplicaClientDto) {
    Div(
        attrs = {
            style {
                width(100.percent)
                height(100.percent)
                display(DisplayStyle.Flex)
                flexFlow(FlexDirection.Column, FlexWrap.Nowrap)
            }
        }
    ) {
        Div(
            attrs = {
                style {
                    width(100.percent)
                    property("flex", "0 1 auto")
                }
            }
        ) {
            NavBar(title = "Replica dev tool")
        }
        Ul(
            attrs = {
                style {
                    width(100.percent)
                    margin(0.px)
                    property("flex", "1 1 auto")
                    property("overflow-y", "scroll")
                }
            }
        ) {
            replicaClientDto.replicas.values.forEach { replica ->
                ReplicaItemUi(item = replica)
            }
        }
    }
}