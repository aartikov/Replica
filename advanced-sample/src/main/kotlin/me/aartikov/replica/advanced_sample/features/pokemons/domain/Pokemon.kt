package me.aartikov.replica.advanced_sample.features.pokemons.domain

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class PokemonId(val value: String)

data class Pokemon(
    val id: PokemonId,
    val name: String
)