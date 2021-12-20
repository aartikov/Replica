package me.aartikov.replica.sample.features.pokemons.ui.list

import me.aartikov.replica.sample.features.pokemons.domain.Pokemon
import me.aartikov.replica.sample.features.pokemons.domain.PokemonId
import me.aartikov.replica.sample.features.pokemons.domain.PokemonType
import me.aartikov.replica.sample.features.pokemons.domain.PokemonTypeId
import me.aartikov.replica.single.Loadable

interface PokemonListComponent {

    val types: List<PokemonType>

    val selectedTypeId: PokemonTypeId

    val pokemonsState: Loadable<List<Pokemon>>

    fun onTypeClick(typeId: PokemonTypeId)

    fun onPokemonClick(pokemonId: PokemonId)

    fun onRefresh()

    fun onRetryClick()

    sealed interface Output {
        data class PokemonDetailsRequested(val pokemonId: PokemonId) : Output
    }
}