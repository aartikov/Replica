package me.aartikov.replica.sample.features.pokemons.domain

import kotlinx.serialization.Serializable

@Serializable
data class DetailedPokemon(
    val id: PokemonId,
    val name: String,
    val height: Float,
    val weight: Float,
    val imageUrl: String,
    val types: List<PokemonType>
)