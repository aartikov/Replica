package me.aartikov.replica.sample.features.menu.ui

interface MenuComponent {

    fun onMenuItemClick(item: MenuItem)

    sealed interface Output {
        data class OpenScreen(val menuItem: MenuItem) : Output
    }
}