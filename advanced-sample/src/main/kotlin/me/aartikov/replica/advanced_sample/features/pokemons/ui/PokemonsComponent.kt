package me.aartikov.replica.advanced_sample.features.pokemons.ui

import com.arkivanov.decompose.router.stack.ChildStack
import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.advanced_sample.features.pokemons.ui.details.PokemonDetailsComponent
import me.aartikov.replica.advanced_sample.features.pokemons.ui.list.PokemonListComponent

interface PokemonsComponent {

    val childStack: StateFlow<ChildStack<*, Child>>

    sealed interface Child {
        class List(val component: PokemonListComponent) : Child
        class Details(val component: PokemonDetailsComponent) : Child
    }
}