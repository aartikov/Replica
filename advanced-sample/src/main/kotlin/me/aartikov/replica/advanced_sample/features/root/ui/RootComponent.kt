package me.aartikov.replica.advanced_sample.features.root.ui

import com.arkivanov.decompose.router.stack.ChildStack
import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.advanced_sample.core.message.ui.MessageComponent
import me.aartikov.replica.advanced_sample.features.dudes.ui.DudesComponent
import me.aartikov.replica.advanced_sample.features.fruits.ui.FruitsComponent
import me.aartikov.replica.advanced_sample.features.menu.ui.MenuComponent
import me.aartikov.replica.advanced_sample.features.pokemons.ui.PokemonsComponent
import me.aartikov.replica.advanced_sample.features.project.ui.ProjectComponent
import me.aartikov.replica.advanced_sample.features.search.ui.SearchComponent

interface RootComponent {

    val childStack: StateFlow<ChildStack<*, Child>>

    val messageComponent: MessageComponent

    sealed interface Child {
        class Menu(val component: MenuComponent) : Child
        class Project(val component: ProjectComponent) : Child
        class Pokemons(val component: PokemonsComponent) : Child
        class Fruits(val component: FruitsComponent) : Child
        class Dudes(val component: DudesComponent) : Child
        class Search(val component: SearchComponent) : Child
    }
}