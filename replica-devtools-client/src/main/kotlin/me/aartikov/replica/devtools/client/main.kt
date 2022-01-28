package me.aartikov.replica.devtools.client

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import me.aartikov.replica.devtools.client.view_data.SortType
import me.aartikov.replica.devtools.client.view_data.toViewData
import me.aartikov.replica.devtools.dto.DtoStore
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement

fun main() {
    val rootElement = document.getElementById("root") as HTMLElement
    val webClient = WebClient()
    val dtoStore = DtoStore()
    val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    coroutineScope.launch { webClient.listenSocket(dtoStore) }

    renderComposable(root = rootElement) {
        val sortType by remember { mutableStateOf(SortType.ByObservingTime) }
        val state by dtoStore.stateDto.collectAsState()
        Body(state.toViewData(sortType))
    }
}