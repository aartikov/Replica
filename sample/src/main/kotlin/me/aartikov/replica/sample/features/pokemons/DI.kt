package me.aartikov.replica.sample.features.pokemons

import com.arkivanov.decompose.ComponentContext
import me.aartikov.replica.algebra.withKey
import me.aartikov.replica.sample.core.data.network.BaseUrl
import me.aartikov.replica.sample.core.data.network.NetworkApiFactory
import me.aartikov.replica.sample.core.ui.ComponentFactory
import me.aartikov.replica.sample.features.pokemons.data.PokemonApi
import me.aartikov.replica.sample.features.pokemons.data.PokemonRepository
import me.aartikov.replica.sample.features.pokemons.data.PokemonRepositoryImpl
import me.aartikov.replica.sample.features.pokemons.data.PokemonStorage
import me.aartikov.replica.sample.features.pokemons.domain.PokemonId
import me.aartikov.replica.sample.features.pokemons.ui.PokemonsComponent
import me.aartikov.replica.sample.features.pokemons.ui.RealPokemonsComponent
import me.aartikov.replica.sample.features.pokemons.ui.details.PokemonDetailsComponent
import me.aartikov.replica.sample.features.pokemons.ui.details.RealPokemonDetailsComponent
import me.aartikov.replica.sample.features.pokemons.ui.list.PokemonListComponent
import me.aartikov.replica.sample.features.pokemons.ui.list.RealPokemonListComponent
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.koin.dsl.module

val pokemonsModule = module {
    single<PokemonApi> {
        get<NetworkApiFactory>(named(BaseUrl.Pokemons)).createApi()
    }
    single<PokemonRepository> {
        PokemonRepositoryImpl(
            get(),
            PokemonStorage(androidContext()),
            get()
        )
    }
}

fun ComponentFactory.createPokemonsComponent(
    componentContext: ComponentContext
): PokemonsComponent {
    return RealPokemonsComponent(componentContext, get())
}

fun ComponentFactory.createPokemonListComponent(
    componentContext: ComponentContext,
    onOutput: (PokemonListComponent.Output) -> Unit
): PokemonListComponent {
    val pokemonsByTypeReplica = get<PokemonRepository>().pokemonsByTypeReplica
    return RealPokemonListComponent(componentContext, onOutput, pokemonsByTypeReplica, get())
}

fun ComponentFactory.createPokemonDetailsComponent(
    componentContext: ComponentContext,
    pokemonId: PokemonId
): PokemonDetailsComponent {
    val pokemonReplica = get<PokemonRepository>().pokemonByIdReplica.withKey(pokemonId)
    return RealPokemonDetailsComponent(componentContext, pokemonReplica, get())
}