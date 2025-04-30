package me.aartikov.replica.simple_sample.core.message.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import me.aartikov.replica.simple_sample.core.message.data.MessageService

class DefaultMessageViewModel(
    messageService: MessageService,
) : MessageViewModel, ViewModel() {

    companion object {
        private const val DISPLAY_DURATION_MS = 4000L
        private const val STOP_TIMEOUT_MS = 5000L
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val visibleMessage = messageService.messageFlow
        .flatMapLatest { message ->
            flow {
                emit(message)
                delay(DISPLAY_DURATION_MS)
                emit(null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = STOP_TIMEOUT_MS),
            initialValue = null
        )
}
