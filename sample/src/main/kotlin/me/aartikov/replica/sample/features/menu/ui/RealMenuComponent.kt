package me.aartikov.replica.sample.features.menu.ui

import com.arkivanov.decompose.ComponentContext

class RealMenuComponent(
    componentContext: ComponentContext,
    val onOutput: (MenuComponent.Output) -> Unit
) : ComponentContext by componentContext, MenuComponent {

    override fun onMenuItemClick(item: MenuItem) {
        onOutput(MenuComponent.Output.OpenScreen(item))
    }
}