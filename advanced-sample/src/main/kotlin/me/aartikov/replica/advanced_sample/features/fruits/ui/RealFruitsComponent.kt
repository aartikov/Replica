package me.aartikov.replica.advanced_sample.features.fruits.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import me.aartikov.replica.advanced_sample.core.ComponentFactory
import me.aartikov.replica.advanced_sample.core.utils.toStateFlow
import me.aartikov.replica.advanced_sample.features.fruits.createFruitsAllComponent
import me.aartikov.replica.advanced_sample.features.fruits.createFruitsFavouritesComponent

class RealFruitsComponent(
    componentContext: ComponentContext,
    private val componentFactory: ComponentFactory
) : ComponentContext by componentContext, FruitsComponent {

    private val navigation = StackNavigation<Config>()

    override val selectedTab = MutableStateFlow(FruitsComponent.Tab.All)

    override val stack: StateFlow<ChildStack<*, FruitsComponent.Child>> = childStack(
        source = navigation,
        handleBackButton = true,
        serializer = Config.serializer(),
        initialConfiguration = Config.AllFruits,
        childFactory = ::createChild
    ).toStateFlow(lifecycle)

    private fun createChild(
        config: Config,
        componentContext: ComponentContext
    ) = when (config) {
        Config.AllFruits -> FruitsComponent.Child.All(
            componentFactory.createFruitsAllComponent(
                componentContext,
            )
        )

        Config.FavouriteFruits -> FruitsComponent.Child.Favourites(
            componentFactory.createFruitsFavouritesComponent(
                componentContext
            )
        )
    }

    override fun onTabClick(tab: FruitsComponent.Tab) {
        selectedTab.update { tab }
        navigation.bringToFront(
            when (tab) {
                FruitsComponent.Tab.All -> Config.AllFruits
                FruitsComponent.Tab.Favourites -> Config.FavouriteFruits
            }
        )
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object AllFruits : Config

        @Serializable
        data object FavouriteFruits : Config
    }
}