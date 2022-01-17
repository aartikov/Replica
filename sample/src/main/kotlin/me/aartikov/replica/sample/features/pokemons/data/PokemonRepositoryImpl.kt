package me.aartikov.replica.sample.features.pokemons.data

import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.keyed.KeyedPhysicalReplica
import me.aartikov.replica.sample.features.pokemons.data.dto.toDomain
import me.aartikov.replica.sample.features.pokemons.domain.DetailedPokemon
import me.aartikov.replica.sample.features.pokemons.domain.Pokemon
import me.aartikov.replica.sample.features.pokemons.domain.PokemonId
import me.aartikov.replica.sample.features.pokemons.domain.PokemonTypeId
import me.aartikov.replica.single.ReplicaSettings
import kotlin.time.Duration.Companion.seconds

class PokemonRepositoryImpl(
    replicaClient: ReplicaClient,
    api: PokemonApi
) : PokemonRepository {

    override val pokemonsByTypeReplica: KeyedPhysicalReplica<PokemonTypeId, List<Pokemon>> =
        replicaClient.createKeyedReplica(
            name = "pokemonsByType",
            childName = { typeId -> typeId.value },
            childSettings = {
                ReplicaSettings(
                    staleTime = 5.seconds,
                    clearTime = 10.seconds
                )
            }
        ) { pokemonTypeId ->
            api.getPokemonsByType(pokemonTypeId.value).toDomain()
        }

    override val pokemonByIdReplica: KeyedPhysicalReplica<PokemonId, DetailedPokemon> =
        replicaClient.createKeyedReplica(
            name = "pokemonById",
            childName = { pokemonId -> pokemonId.value },
            childSettings = {
                ReplicaSettings(
                    staleTime = 5.seconds,
                    clearTime = 10.seconds
                )
            }
        ) { pokemonId ->
            api.getPokemonById(pokemonId.value).toDomain()
        }
}