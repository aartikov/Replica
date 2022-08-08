package me.aartikov.replica.advanced_sample.features.message.ui

import me.aartikov.replica.advanced_sample.core.message.domain.Message


interface MessageComponent {

    val visibleMessage: Message?
}