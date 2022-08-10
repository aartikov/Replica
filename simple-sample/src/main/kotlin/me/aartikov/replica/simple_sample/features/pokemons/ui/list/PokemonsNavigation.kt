package me.aartikov.replica.simple_sample.features.pokemons.ui.list

import me.aartikov.replica.simple_sample.features.pokemons.domain.PokemonId

interface PokemonsNavigation {

    fun navigateToDetails(pokemonId: PokemonId)

    fun navigateBack(): Boolean
}