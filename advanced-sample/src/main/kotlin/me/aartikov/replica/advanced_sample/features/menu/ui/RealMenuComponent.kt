package me.aartikov.replica.advanced_sample.features.menu.ui

import com.arkivanov.decompose.ComponentContext

class RealMenuComponent(
    componentContext: ComponentContext,
    val onOutput: (MenuComponent.Output) -> Unit
) : ComponentContext by componentContext, MenuComponent {

    override fun onMenuItemClick(item: MenuItem) {
        onOutput(MenuComponent.Output.MenuItemSelected(item))
    }
}