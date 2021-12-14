package me.aartikov.replica.sample.features.message

import com.arkivanov.decompose.ComponentContext
import me.aartikov.replica.sample.core.ui.ComponentFactory
import me.aartikov.replica.sample.features.message.ui.MessageComponent
import me.aartikov.replica.sample.features.message.ui.RealMessageComponent
import org.koin.core.component.get

fun ComponentFactory.createMessagesComponent(
    componentContext: ComponentContext
): MessageComponent {
    return RealMessageComponent(componentContext, get())
}