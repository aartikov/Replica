package me.aartikov.replica.advanced_sample.features.fruits.ui

import com.arkivanov.decompose.router.stack.ChildStack
import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.advanced_sample.R
import me.aartikov.replica.advanced_sample.features.fruits.ui.all.FruitsAllComponent
import me.aartikov.replica.advanced_sample.features.fruits.ui.favourites.FruitsFavouritesComponent

interface FruitsComponent {

    val stack: StateFlow<ChildStack<*, Child>>
    val selectedTab: StateFlow<Tab>

    fun onTabClick(tab: Tab)

    enum class Tab(val resId: Int) {
        All(R.string.fruits_all),
        Favourites(R.string.fruits_favourites)
    }

    sealed interface Child {
        data class Favourites(val component: FruitsFavouritesComponent) : Child
        data class All(val component: FruitsAllComponent) : Child
    }
}