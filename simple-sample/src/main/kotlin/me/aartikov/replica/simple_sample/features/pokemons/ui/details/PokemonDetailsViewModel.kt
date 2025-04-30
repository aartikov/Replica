package me.aartikov.replica.simple_sample.features.pokemons.ui.details

import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.simple_sample.features.pokemons.domain.DetailedPokemon
import me.aartikov.replica.single.Loadable
import me.aartikov.replica.view_model.Activable

interface PokemonDetailsViewModel : Activable {

    val pokemonState: StateFlow<Loadable<DetailedPokemon>>

    fun onPokemonImageClick(name: String)

    fun onRefresh()

    fun onRetryClick()
}
