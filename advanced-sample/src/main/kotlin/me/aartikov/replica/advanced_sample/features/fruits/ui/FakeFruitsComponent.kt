package me.aartikov.replica.advanced_sample.features.fruits.ui

import com.arkivanov.decompose.router.stack.ChildStack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.advanced_sample.core.utils.createFakeChildStack
import me.aartikov.replica.advanced_sample.features.fruits.ui.FruitsComponent.Child
import me.aartikov.replica.advanced_sample.features.fruits.ui.FruitsComponent.Tab
import me.aartikov.replica.advanced_sample.features.fruits.ui.favourites.FakeFruitsFavouritesComponent

class FakeFruitsComponent : FruitsComponent {
    override val stack: StateFlow<ChildStack<*, Child>> =
        MutableStateFlow(
            createFakeChildStack(
                Child.Favourites(
                    FakeFruitsFavouritesComponent()
                )
            )
        )

    override val selectedTab: StateFlow<Tab> = MutableStateFlow(Tab.All)

    override fun onTabClick(tab: Tab): Unit = Unit
}
