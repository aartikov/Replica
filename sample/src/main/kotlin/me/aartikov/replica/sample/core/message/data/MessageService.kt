package me.aartikov.replica.sample.core.message.data

import kotlinx.coroutines.flow.Flow
import me.aartikov.replica.sample.core.message.domain.Message

/**
 * A service for centralized message showing
 */
interface MessageService {

    val messageFlow: Flow<Message>

    fun showMessage(message: Message)
}