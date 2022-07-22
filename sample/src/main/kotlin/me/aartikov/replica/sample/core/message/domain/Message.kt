package me.aartikov.replica.sample.core.message.domain

import androidx.annotation.DrawableRes
import me.aartikov.sesame.localizedstring.LocalizedString

data class Message(
    val text: LocalizedString,
    @DrawableRes val iconRes: Int? = null,
    val actionTitle: LocalizedString? = null,
    val action: (() -> Unit)? = null
)