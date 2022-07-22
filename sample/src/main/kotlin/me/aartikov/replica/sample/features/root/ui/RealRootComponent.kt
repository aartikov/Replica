package me.aartikov.replica.sample.features.root.ui

import android.os.Parcelable
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.push
import kotlinx.parcelize.Parcelize
import me.aartikov.replica.sample.core.ComponentFactory
import me.aartikov.replica.sample.core.utils.toComposeState
import me.aartikov.replica.sample.features.dudes.createDudesComponent
import me.aartikov.replica.sample.features.fruits.createFruitsComponent
import me.aartikov.replica.sample.features.menu.createMenuComponent
import me.aartikov.replica.sample.features.menu.ui.MenuComponent
import me.aartikov.replica.sample.features.menu.ui.MenuItem
import me.aartikov.replica.sample.features.message.createMessagesComponent
import me.aartikov.replica.sample.features.pokemons.createPokemonsComponent
import me.aartikov.replica.sample.features.project.createProjectComponent

class RealRootComponent(
    componentContext: ComponentContext,
    private val componentFactory: ComponentFactory
) : ComponentContext by componentContext, RootComponent {

    private val navigation = StackNavigation<ChildConfig>()

    override val childStack: ChildStack<*, RootComponent.Child> by childStack(
        source = navigation,
        initialConfiguration = ChildConfig.Menu,
        handleBackButton = true,
        childFactory = ::createChild
    ).toComposeState(lifecycle)

    override val messageComponent = componentFactory.createMessagesComponent(
        childContext(key = "message")
    )

    private fun createChild(config: ChildConfig, componentContext: ComponentContext) =
        when (config) {
            is ChildConfig.Menu -> {
                RootComponent.Child.Menu(
                    componentFactory.createMenuComponent(componentContext, ::onMenuOutput)
                )
            }

            is ChildConfig.Project -> {
                RootComponent.Child.Project(
                    componentFactory.createProjectComponent(componentContext)
                )
            }

            is ChildConfig.Pokemons -> {
                RootComponent.Child.Pokemons(
                    componentFactory.createPokemonsComponent(componentContext)
                )
            }

            is ChildConfig.Fruits -> {
                RootComponent.Child.Fruits(
                    componentFactory.createFruitsComponent(componentContext)
                )
            }

            is ChildConfig.Dudes -> {
                RootComponent.Child.Dudes(
                    componentFactory.createDudesComponent(componentContext)
                )
            }
        }

    private fun onMenuOutput(output: MenuComponent.Output): Unit = when (output) {
        is MenuComponent.Output.MenuItemSelected -> {
            val config = when (output.menuItem) {
                MenuItem.Project -> ChildConfig.Project
                MenuItem.Pokemons -> ChildConfig.Pokemons
                MenuItem.Fruits -> ChildConfig.Fruits
                MenuItem.Dudes -> ChildConfig.Dudes
            }
            navigation.push(config)
        }
    }

    private sealed interface ChildConfig : Parcelable {

        @Parcelize
        object Menu : ChildConfig

        @Parcelize
        object Project : ChildConfig

        @Parcelize
        object Pokemons : ChildConfig

        @Parcelize
        object Fruits : ChildConfig

        @Parcelize
        object Dudes : ChildConfig
    }
}

