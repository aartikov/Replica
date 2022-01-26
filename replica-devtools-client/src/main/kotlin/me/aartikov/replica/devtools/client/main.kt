package me.aartikov.replica.devtools.client

import androidx.compose.runtime.*
import kotlinx.browser.document
import kotlinx.coroutines.launch
import me.aartikov.replica.devtools.dto.DtoStore
import me.aartikov.replica.devtools.dto.ReplicaClientDto
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement

fun main() {
    val rootElement = document.getElementById("root") as HTMLElement
    val webClient = WebClient()

    renderComposable(root = rootElement) {
        var state by remember { mutableStateOf(ReplicaClientDto()) }
        val dtoStore = DtoStore { state = it }

        rememberCoroutineScope().launch { webClient.listenSocket(dtoStore) }

        Body(state)
    }
}