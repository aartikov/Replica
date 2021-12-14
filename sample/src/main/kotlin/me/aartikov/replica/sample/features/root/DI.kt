package me.aartikov.replica.sample.features.root

import com.arkivanov.decompose.ComponentContext
import me.aartikov.replica.sample.core.ui.ComponentFactory
import me.aartikov.replica.sample.features.root.ui.RealRootComponent
import me.aartikov.replica.sample.features.root.ui.RootComponent
import org.koin.core.component.get

fun ComponentFactory.createRootComponent(componentContext: ComponentContext): RootComponent {
    return RealRootComponent(componentContext, get())
}