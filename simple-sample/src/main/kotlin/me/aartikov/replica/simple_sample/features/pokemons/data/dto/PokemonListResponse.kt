package me.aartikov.replica.simple_sample.features.pokemons.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.aartikov.replica.simple_sample.features.pokemons.domain.Pokemon

@Serializable
class PokemonListResponse(
    @SerialName("pokemon") val pokemons: List<PokemonWrapperResponse>
)

fun PokemonListResponse.toDomain(): List<Pokemon> {
    return pokemons.map { it.pokemon.toDomain() }
}
