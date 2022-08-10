package me.aartikov.replica.simple_sample.features.pokemons.data

import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.keyed.KeyedPhysicalReplica
import me.aartikov.replica.keyed.KeyedReplicaSettings
import me.aartikov.replica.simple_sample.features.pokemons.data.dto.toDomain
import me.aartikov.replica.simple_sample.features.pokemons.domain.DetailedPokemon
import me.aartikov.replica.simple_sample.features.pokemons.domain.Pokemon
import me.aartikov.replica.simple_sample.features.pokemons.domain.PokemonId
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaSettings
import kotlin.time.Duration.Companion.seconds

class PokemonRepositoryImpl(
    replicaClient: ReplicaClient,
    api: PokemonApi
) : PokemonRepository {

    override val pokemonsReplica: PhysicalReplica<List<Pokemon>> =
        replicaClient.createReplica(
            name = "pokemons",
            settings = ReplicaSettings(staleTime = 10.seconds),
            fetcher = {
                api.getPokemons().toDomain()
            }
        )

    override val pokemonByIdReplica: KeyedPhysicalReplica<PokemonId, DetailedPokemon> =
        replicaClient.createKeyedReplica(
            name = "pokemonById",
            childName = { pokemonId -> "pokemonId = ${pokemonId.value}" },
            settings = KeyedReplicaSettings(maxCount = 5),
            childSettings = {
                ReplicaSettings(
                    staleTime = 10.seconds,
                    clearTime = 60.seconds
                )
            },
            fetcher = { pokemonId ->
                api.getPokemonById(pokemonId.value).toDomain()
            }
        )
}