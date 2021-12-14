package me.aartikov.replica.sample.core.ui.message

import me.aartikov.sesame.localizedstring.LocalizedString
import java.util.*

data class MessageData(
    val text: LocalizedString,
    val id: UUID = UUID.randomUUID()
)