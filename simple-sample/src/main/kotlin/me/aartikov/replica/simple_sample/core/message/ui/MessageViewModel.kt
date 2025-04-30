package me.aartikov.replica.simple_sample.core.message.ui

import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.simple_sample.core.message.domain.Message

interface MessageViewModel {

    val visibleMessage: StateFlow<Message?>
}
