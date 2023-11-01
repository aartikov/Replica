package me.aartikov.replica.advanced_sample.features.pokemons.ui.details

import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.advanced_sample.features.pokemons.domain.DetailedPokemon
import me.aartikov.replica.single.Loadable

interface PokemonDetailsComponent {

    val pokemonState: StateFlow<Loadable<DetailedPokemon>>

    fun onRefresh()

    fun onRetryClick()
}