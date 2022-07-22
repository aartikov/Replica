package me.aartikov.replica.sample.core.message.data

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import me.aartikov.replica.sample.core.message.domain.Message

class MessageServiceImpl : MessageService {

    private val messageChannel = Channel<Message>(Channel.UNLIMITED)

    override val messageFlow = messageChannel.receiveAsFlow()

    override fun showMessage(message: Message) {
        messageChannel.trySend(message)
    }
}