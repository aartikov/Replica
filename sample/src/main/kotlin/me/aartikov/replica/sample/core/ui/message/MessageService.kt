package me.aartikov.replica.sample.core.ui.message

import kotlinx.coroutines.flow.Flow

interface MessageService {

    val messageFlow: Flow<MessageData>

    fun showMessage(message: MessageData)
}