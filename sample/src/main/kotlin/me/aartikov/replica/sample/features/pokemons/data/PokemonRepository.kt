package me.aartikov.replica.sample.features.pokemons.data

import me.aartikov.replica.keyed.KeyedReplica
import me.aartikov.replica.sample.features.pokemons.domain.DetailedPokemon
import me.aartikov.replica.sample.features.pokemons.domain.Pokemon
import me.aartikov.replica.sample.features.pokemons.domain.PokemonId
import me.aartikov.replica.sample.features.pokemons.domain.PokemonTypeId

interface PokemonRepository {

    val pokemonsByTypeReplica: KeyedReplica<PokemonTypeId, List<Pokemon>>

    val pokemonByIdReplica: KeyedReplica<PokemonId, DetailedPokemon>
}