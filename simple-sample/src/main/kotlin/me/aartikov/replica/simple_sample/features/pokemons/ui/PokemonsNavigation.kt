package me.aartikov.replica.simple_sample.features.pokemons.ui

import me.aartikov.replica.simple_sample.features.pokemons.domain.PokemonId

interface PokemonsNavigation {
    fun navigateToDetails(pokemonId: PokemonId)
}