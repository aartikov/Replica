package me.aartikov.replica.sample.features.pokemons.domain


@JvmInline
value class PokemonTypeId(val value: String)

data class PokemonType(val id: PokemonTypeId, val name: String) {
    companion object {
        val Fire = PokemonType(id = PokemonTypeId("10"), name = "Fire")
        val Water = PokemonType(id = PokemonTypeId("11"), name = "Water")
        val Electric = PokemonType(id = PokemonTypeId("13"), name = "Electric")
        val Grass = PokemonType(id = PokemonTypeId("12"), name = "Grass")
        val Poison = PokemonType(id = PokemonTypeId("4"), name = "Poison")
    }
}