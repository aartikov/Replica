package me.aartikov.replica.simple_sample.features.pokemons.data

import me.aartikov.replica.keyed.KeyedReplica
import me.aartikov.replica.simple_sample.features.pokemons.domain.DetailedPokemon
import me.aartikov.replica.simple_sample.features.pokemons.domain.Pokemon
import me.aartikov.replica.simple_sample.features.pokemons.domain.PokemonId
import me.aartikov.replica.single.Replica

interface PokemonRepository {

    val pokemonsReplica: Replica<List<Pokemon>>

    val pokemonByIdReplica: KeyedReplica<PokemonId, DetailedPokemon>
}