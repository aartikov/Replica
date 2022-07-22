package me.aartikov.replica.sample.features.message.ui

import me.aartikov.replica.sample.core.message.domain.Message


interface MessageComponent {

    val visibleMessage: Message?
}