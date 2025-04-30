package me.aartikov.replica.simple_sample.features.pokemons.domain

data class DetailedPokemon(
    val id: PokemonId,
    val name: String,
    val height: Float,
    val weight: Float,
    val imageUrl: String,
) {
    companion object {
        val MOCK = DetailedPokemon(
            id = PokemonId("1"),
            name = "Bulbasaur",
            height = 0.7f,
            weight = 6.9f,
            imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png"
        )
    }
}
