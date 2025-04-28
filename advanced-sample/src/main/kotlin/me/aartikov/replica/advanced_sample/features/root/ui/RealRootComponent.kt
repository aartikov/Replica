package me.aartikov.replica.advanced_sample.features.root.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pushNew
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import me.aartikov.replica.advanced_sample.core.ComponentFactory
import me.aartikov.replica.advanced_sample.core.createMessageComponent
import me.aartikov.replica.advanced_sample.core.utils.toStateFlow
import me.aartikov.replica.advanced_sample.features.dudes.createDudesComponent
import me.aartikov.replica.advanced_sample.features.fruits.createFruitsComponent
import me.aartikov.replica.advanced_sample.features.menu.createMenuComponent
import me.aartikov.replica.advanced_sample.features.menu.ui.MenuComponent
import me.aartikov.replica.advanced_sample.features.menu.ui.MenuItem
import me.aartikov.replica.advanced_sample.features.pokemons.createPokemonsComponent
import me.aartikov.replica.advanced_sample.features.project.createProjectComponent
import me.aartikov.replica.advanced_sample.features.root.ui.RootComponent.Child.Dudes
import me.aartikov.replica.advanced_sample.features.root.ui.RootComponent.Child.Fruits
import me.aartikov.replica.advanced_sample.features.root.ui.RootComponent.Child.Menu
import me.aartikov.replica.advanced_sample.features.root.ui.RootComponent.Child.Pokemons
import me.aartikov.replica.advanced_sample.features.root.ui.RootComponent.Child.Project
import me.aartikov.replica.advanced_sample.features.root.ui.RootComponent.Child.Search
import me.aartikov.replica.advanced_sample.features.search.createSearchComponent

class RealRootComponent(
    componentContext: ComponentContext,
    private val componentFactory: ComponentFactory,
) : ComponentContext by componentContext, RootComponent {

    private val navigation = StackNavigation<ChildConfig>()

    override val childStack: StateFlow<ChildStack<*, RootComponent.Child>> = childStack(
        source = navigation,
        initialConfiguration = ChildConfig.Menu,
        handleBackButton = true,
        serializer = ChildConfig.serializer(),
        childFactory = ::createChild
    ).toStateFlow(lifecycle)

    override val messageComponent = componentFactory.createMessageComponent(
        childContext(key = "message")
    )

    private fun createChild(config: ChildConfig, componentContext: ComponentContext) =
        when (config) {
            ChildConfig.Menu -> {
                Menu(
                    componentFactory.createMenuComponent(componentContext, ::onMenuOutput)
                )
            }

            ChildConfig.Project -> {
                Project(
                    componentFactory.createProjectComponent(componentContext)
                )
            }

            ChildConfig.Pokemons -> {
                Pokemons(
                    componentFactory.createPokemonsComponent(componentContext)
                )
            }

            ChildConfig.Fruits -> {
                Fruits(
                    componentFactory.createFruitsComponent(componentContext)
                )
            }

            ChildConfig.Dudes -> {
                Dudes(
                    componentFactory.createDudesComponent(componentContext)
                )
            }

            ChildConfig.Search -> {
                Search(
                    componentFactory.createSearchComponent(componentContext)
                )
            }
        }

    private fun onMenuOutput(output: MenuComponent.Output): Unit = when (output) {
        is MenuComponent.Output.MenuItemSelected -> when (output.menuItem) {
            MenuItem.Project -> ChildConfig.Project
            MenuItem.Pokemons -> ChildConfig.Pokemons
            MenuItem.Fruits -> ChildConfig.Fruits
            MenuItem.Dudes -> ChildConfig.Dudes
            MenuItem.Search -> ChildConfig.Search
        }.run(navigation::pushNew)
    }

    @Serializable
    private sealed interface ChildConfig {

        @Serializable
        data object Menu : ChildConfig

        @Serializable
        data object Project : ChildConfig

        @Serializable
        data object Pokemons : ChildConfig

        @Serializable
        data object Fruits : ChildConfig

        @Serializable
        data object Dudes : ChildConfig

        @Serializable
        data object Search : ChildConfig
    }
}
