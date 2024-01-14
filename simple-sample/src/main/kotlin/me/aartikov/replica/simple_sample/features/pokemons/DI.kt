package me.aartikov.replica.simple_sample.features.pokemons

import me.aartikov.replica.algebra.normal.withKey
import me.aartikov.replica.algebra.paged.withKey
import me.aartikov.replica.simple_sample.core.network.NetworkApiFactory
import me.aartikov.replica.simple_sample.features.pokemons.data.PokemonApi
import me.aartikov.replica.simple_sample.features.pokemons.data.PokemonRepository
import me.aartikov.replica.simple_sample.features.pokemons.data.PokemonRepositoryImpl
import me.aartikov.replica.simple_sample.features.pokemons.domain.PokemonId
import me.aartikov.replica.simple_sample.features.pokemons.ui.details.PokemonDetailsViewModel
import me.aartikov.replica.simple_sample.features.pokemons.ui.list.PokemonListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val pokemonsModule = module {
    single<PokemonApi> { get<NetworkApiFactory>().createApi() }
    single<PokemonRepository> { PokemonRepositoryImpl(get(), get()) }

    viewModel {
        val pokemonsReplica = get<PokemonRepository>().pokemonsReplica
        PokemonListViewModel(pokemonsReplica, get())
    }

    viewModel { params ->
        val pokemonId = params.get<PokemonId>()
        val pokemonReplica = get<PokemonRepository>().pokemonByIdReplica.withKey(pokemonId)
        PokemonDetailsViewModel(pokemonReplica, get())
    }
}