package me.aartikov.replica.simple_sample.core.message.ui

import kotlinx.coroutines.flow.MutableStateFlow
import me.aartikov.replica.simple_sample.core.message.domain.Message
import me.aartikov.sesame.localizedstring.LocalizedString

class PreviewMessageViewModel : MessageViewModel {

    override val visibleMessage = MutableStateFlow(Message(LocalizedString.raw("Preview")))
}
