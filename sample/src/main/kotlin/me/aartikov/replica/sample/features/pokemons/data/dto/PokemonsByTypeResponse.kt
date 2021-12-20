package me.aartikov.replica.sample.features.pokemons.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.aartikov.replica.sample.features.pokemons.data.dto.PokemonWrapperResponse
import me.aartikov.replica.sample.features.pokemons.data.dto.toDomain
import me.aartikov.replica.sample.features.pokemons.domain.Pokemon

@Serializable
class PokemonsByTypeResponse(
    @SerialName("pokemon") val pokemons: List<PokemonWrapperResponse>
)

fun PokemonsByTypeResponse.toDomain(): List<Pokemon> {
    return pokemons.map { it.pokemon.toDomain() }
}
