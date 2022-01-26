package me.aartikov.replica.sample.features.root.ui

import android.os.Parcelable
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.RouterState
import com.arkivanov.decompose.router.push
import com.arkivanov.decompose.router.router
import kotlinx.parcelize.Parcelize
import me.aartikov.replica.sample.core.ui.ComponentFactory
import me.aartikov.replica.sample.core.ui.utils.toComposeState
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

    private val router = router<ChildConfig, RootComponent.Child>(
        initialConfiguration = ChildConfig.Menu,
        handleBackButton = true,
        childFactory = ::createChild
    )

    override val routerState: RouterState<*, RootComponent.Child>
        by router.state.toComposeState(lifecycle)

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
        is MenuComponent.Output.MenuItemSelected -> when (output.menuItem) {
            MenuItem.Project -> router.push(ChildConfig.Project)
            MenuItem.Pokemons -> router.push(ChildConfig.Pokemons)
            MenuItem.Fruits -> router.push(ChildConfig.Fruits)
            MenuItem.Dudes -> router.push(ChildConfig.Dudes)
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

