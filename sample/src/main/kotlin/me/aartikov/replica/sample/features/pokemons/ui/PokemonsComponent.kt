package me.aartikov.replica.sample.features.pokemons.ui

import com.arkivanov.decompose.router.stack.ChildStack
import me.aartikov.replica.sample.features.pokemons.ui.details.PokemonDetailsComponent
import me.aartikov.replica.sample.features.pokemons.ui.list.PokemonListComponent

interface PokemonsComponent {

    val childStack: ChildStack<*, Child>

    sealed interface Child {
        class List(val component: PokemonListComponent) : Child
        class Details(val component: PokemonDetailsComponent) : Child
    }
}