package me.aartikov.replica.simple_sample.features.pokemons

import me.aartikov.replica.simple_sample.core.network.NetworkApiFactory
import me.aartikov.replica.simple_sample.features.pokemons.data.PokemonApi
import me.aartikov.replica.simple_sample.features.pokemons.data.PokemonRepository
import me.aartikov.replica.simple_sample.features.pokemons.data.PokemonRepositoryImpl
import me.aartikov.replica.simple_sample.features.pokemons.ui.details.DefaultPokemonDetailsViewModel
import me.aartikov.replica.simple_sample.features.pokemons.ui.list.DefaultPokemonListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val pokemonsModule = module {
    single<PokemonApi> { get<NetworkApiFactory>().createApi() }
    single<PokemonRepository> { PokemonRepositoryImpl(get(), get()) }

    viewModel {
        DefaultPokemonListViewModel(get(), get())
    }

    viewModel { params ->
        DefaultPokemonDetailsViewModel(params.get(), get(), get(), get())
    }
}
