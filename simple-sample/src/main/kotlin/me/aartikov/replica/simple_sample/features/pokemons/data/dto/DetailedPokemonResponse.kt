package me.aartikov.replica.simple_sample.features.pokemons.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.aartikov.replica.simple_sample.features.pokemons.domain.DetailedPokemon
import me.aartikov.replica.simple_sample.features.pokemons.domain.PokemonId

@Serializable
class DetailedPokemonResponse(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("height") val height: Int,
    @SerialName("weight") val weight: Int
)

fun DetailedPokemonResponse.toDomain(): DetailedPokemon {
    return DetailedPokemon(
        id = PokemonId(id),
        name = formatName(name),
        height = decimetresToMeters(height),
        weight = hectogramsToKilograms(weight),
        imageUrl = getImageUrl(id)
    )
}