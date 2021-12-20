package me.aartikov.replica.sample.features.pokemons.ui

import com.arkivanov.decompose.router.RouterState
import me.aartikov.replica.sample.features.pokemons.ui.details.PokemonDetailsComponent
import me.aartikov.replica.sample.features.pokemons.ui.list.PokemonListComponent

interface PokemonsComponent {

    val routerState: RouterState<*, Child>

    sealed interface Child {
        class List(val component: PokemonListComponent) : Child
        class Details(val component: PokemonDetailsComponent) : Child
    }
}