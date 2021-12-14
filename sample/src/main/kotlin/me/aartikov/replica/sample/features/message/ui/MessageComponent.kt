package me.aartikov.replica.sample.features.message.ui

import me.aartikov.replica.sample.core.ui.message.MessageData
import me.aartikov.sesame.dialog.DialogControl

interface MessageComponent {
    val dialogControl: DialogControl<MessageData, Unit>
}