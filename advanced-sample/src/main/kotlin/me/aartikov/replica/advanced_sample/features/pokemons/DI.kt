package me.aartikov.replica.advanced_sample.features.pokemons

import com.arkivanov.decompose.ComponentContext
import me.aartikov.replica.advanced_sample.core.ComponentFactory
import me.aartikov.replica.advanced_sample.core.network.BaseUrl
import me.aartikov.replica.advanced_sample.core.network.NetworkApiFactory
import me.aartikov.replica.advanced_sample.features.pokemons.data.PokemonApi
import me.aartikov.replica.advanced_sample.features.pokemons.data.PokemonRepository
import me.aartikov.replica.advanced_sample.features.pokemons.data.PokemonRepositoryImpl
import me.aartikov.replica.advanced_sample.features.pokemons.domain.PokemonId
import me.aartikov.replica.advanced_sample.features.pokemons.ui.PokemonsComponent
import me.aartikov.replica.advanced_sample.features.pokemons.ui.RealPokemonsComponent
import me.aartikov.replica.advanced_sample.features.pokemons.ui.details.PokemonDetailsComponent
import me.aartikov.replica.advanced_sample.features.pokemons.ui.details.RealPokemonDetailsComponent
import me.aartikov.replica.advanced_sample.features.pokemons.ui.list.PokemonListComponent
import me.aartikov.replica.advanced_sample.features.pokemons.ui.list.RealPokemonListComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.koin.dsl.module

val pokemonsModule = module {
    single<PokemonApi> {
        get<NetworkApiFactory>(named(BaseUrl.Pokemons)).createApi()
    }
    single<PokemonRepository> { PokemonRepositoryImpl(get(), get()) }
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
    return RealPokemonListComponent(componentContext, onOutput, get(), get())
}

fun ComponentFactory.createPokemonDetailsComponent(
    componentContext: ComponentContext,
    pokemonId: PokemonId
): PokemonDetailsComponent {
    return RealPokemonDetailsComponent(componentContext, pokemonId, get(), get(), get())
}