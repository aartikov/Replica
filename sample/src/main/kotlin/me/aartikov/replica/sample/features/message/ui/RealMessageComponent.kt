package me.aartikov.replica.sample.features.message.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnCreate
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.aartikov.replica.sample.core.ui.message.MessageData
import me.aartikov.replica.sample.core.ui.message.MessageService
import me.aartikov.replica.sample.core.ui.utils.componentCoroutineScope
import me.aartikov.sesame.dialog.DialogControl

class RealMessageComponent(
    componentContext: ComponentContext,
    private val messageService: MessageService,
) : ComponentContext by componentContext, MessageComponent {

    private val coroutineScope = componentCoroutineScope()

    override val dialogControl = DialogControl<MessageData, Unit>()

    init {
        lifecycle.doOnCreate(::collectMessages)
    }

    private fun collectMessages() {
        coroutineScope.launch {
            messageService.messageFlow.collect { messageData ->
                dialogControl.show(messageData)
            }
        }
    }
}