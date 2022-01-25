package me.aartikov.replica.sample.features.message.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnCreate
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.aartikov.replica.sample.core.ui.message.MessageData
import me.aartikov.replica.sample.core.ui.message.MessageService
import me.aartikov.replica.sample.core.ui.utils.componentCoroutineScope

class RealMessageComponent(
    componentContext: ComponentContext,
    private val messageService: MessageService,
) : ComponentContext by componentContext, MessageComponent {

    companion object {
        private const val ShowTime = 4000L
    }

    private val coroutineScope = componentCoroutineScope()

    override var visibleMessageData by mutableStateOf<MessageData?>(null)
        private set

    private var autoDismissJob: Job? = null

    init {
        lifecycle.doOnCreate(::collectMessages)
    }

    private fun collectMessages() {
        coroutineScope.launch {
            messageService.messageFlow.collect { messageData ->
                showMessage(messageData)
            }
        }
    }

    private fun showMessage(messageData: MessageData) {
        autoDismissJob?.cancel()
        visibleMessageData = messageData
        autoDismissJob = coroutineScope.launch {
            delay(ShowTime)
            visibleMessageData = null
        }
    }
}