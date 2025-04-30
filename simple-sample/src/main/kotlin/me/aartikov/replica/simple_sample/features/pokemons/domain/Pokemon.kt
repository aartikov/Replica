package me.aartikov.replica.simple_sample.features.pokemons.domain

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class PokemonId(val value: String)

data class Pokemon(
    val id: PokemonId,
    val name: String,
) {
    companion object {
        val MOCKS = listOf(
            Pokemon(PokemonId("1"), "Bulbasaur"),
            Pokemon(PokemonId("2"), "Ivysaur"),
            Pokemon(PokemonId("3"), "Venusaur"),
            Pokemon(PokemonId("4"), "Charmander"),
            Pokemon(PokemonId("5"), "Charmeleon"),
            Pokemon(PokemonId("6"), "Charizard"),
            Pokemon(PokemonId("7"), "Squirtle"),
            Pokemon(PokemonId("8"), "Wartortle"),
            Pokemon(PokemonId("9"), "Blastoise"),
            Pokemon(PokemonId("10"), "Caterpie")
        )
    }
}
