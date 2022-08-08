package me.aartikov.replica.advanced_sample.core.message.ui

import me.aartikov.replica.advanced_sample.core.message.domain.Message

/**
 * A component for centralized message showing. There should be only one instance
 * of this component in the app connected to the root component.
 */
interface MessageComponent {

    val visibleMessage: Message?

    fun onActionClick()
}