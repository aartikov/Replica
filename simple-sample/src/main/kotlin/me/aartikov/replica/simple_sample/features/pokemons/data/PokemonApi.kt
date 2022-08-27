package me.aartikov.replica.simple_sample.features.pokemons.data

import me.aartikov.replica.simple_sample.features.pokemons.data.dto.DetailedPokemonResponse
import me.aartikov.replica.simple_sample.features.pokemons.data.dto.PokemonListResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface PokemonApi {

    @GET("/api/v2/type/fire")
    suspend fun getPokemons(): PokemonListResponse

    @GET("/api/v2/pokemon/{pokemonId}")
    suspend fun getPokemonById(@Path("pokemonId") pokemonId: String): DetailedPokemonResponse
}