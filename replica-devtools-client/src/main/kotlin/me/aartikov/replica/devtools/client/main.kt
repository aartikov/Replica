package me.aartikov.replica.devtools.client

import androidx.compose.runtime.*
import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable

val client = HttpClient {
    install(WebSockets) {
        // Configure WebSockets
    }
}

val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

fun main() {
    coroutineScope.launch {
        client.webSocket(
            method = HttpMethod.Get,
            host = "192.168.0.8",
            port = 8080,
            path = "/ws"
        ) {
            for (frame in this.incoming) {
                if (frame is Frame.Text) {
                    println(frame.readText())
                    renderComposable(rootElementId = "root") {
                        Text(frame.readText())
                    }
                }
            }
        }
    }
    // renderComposable(rootElementId = "root") {
    //     Body()
    // }
}

@Composable
fun Body() {
    var counter by remember { mutableStateOf(0) }
    Div {
        Text("Clicked: $counter")
    }
    Button(
        attrs = {
            onClick {
                counter++
            }
        }
    ) {
        Text("Click")
    }
}
