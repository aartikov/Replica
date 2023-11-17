package me.aartikov.replica.advanced_sample.features.pokemons.ui.list

import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.advanced_sample.features.pokemons.domain.Pokemon
import me.aartikov.replica.advanced_sample.features.pokemons.domain.PokemonId
import me.aartikov.replica.advanced_sample.features.pokemons.domain.PokemonType
import me.aartikov.replica.advanced_sample.features.pokemons.domain.PokemonTypeId
import me.aartikov.replica.single.Loadable

interface PokemonListComponent {

    val types: List<PokemonType>

    val selectedTypeId:  StateFlow<PokemonTypeId>

    val pokemonsState: StateFlow<Loadable<List<Pokemon>>>

    fun onTypeClick(typeId: PokemonTypeId)

    fun onPokemonClick(pokemonId: PokemonId)

    fun onRefresh()

    fun onRetryClick()

    sealed interface Output {
        data class PokemonDetailsRequested(val pokemonId: PokemonId) : Output
    }
}