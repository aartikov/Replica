package me.aartikov.replica.sample.features.menu

import com.arkivanov.decompose.ComponentContext
import me.aartikov.replica.sample.core.ComponentFactory
import me.aartikov.replica.sample.features.menu.ui.MenuComponent
import me.aartikov.replica.sample.features.menu.ui.RealMenuComponent

fun ComponentFactory.createMenuComponent(
    componentContext: ComponentContext,
    onOutput: (MenuComponent.Output) -> Unit
): MenuComponent {
    return RealMenuComponent(componentContext, onOutput)
}