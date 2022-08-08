package me.aartikov.replica.advanced_sample.core.message

import com.arkivanov.decompose.ComponentContext
import me.aartikov.replica.advanced_sample.core.ComponentFactory
import me.aartikov.replica.advanced_sample.core.message.ui.MessageComponent
import me.aartikov.replica.advanced_sample.core.message.ui.RealMessageComponent
import org.koin.core.component.get

fun ComponentFactory.createMessagesComponent(
    componentContext: ComponentContext
): MessageComponent {
    return RealMessageComponent(componentContext, get())
}