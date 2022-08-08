package me.aartikov.replica.advanced_sample.features.pokemons.ui.details

import me.aartikov.replica.advanced_sample.features.pokemons.domain.DetailedPokemon
import me.aartikov.replica.single.Loadable

interface PokemonDetailsComponent {

    val pokemonState: Loadable<DetailedPokemon>

    fun onRefresh()

    fun onRetryClick()
}