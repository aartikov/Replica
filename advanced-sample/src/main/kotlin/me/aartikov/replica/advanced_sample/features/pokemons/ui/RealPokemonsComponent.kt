package me.aartikov.replica.advanced_sample.features.pokemons.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pushNew
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import me.aartikov.replica.advanced_sample.core.ComponentFactory
import me.aartikov.replica.advanced_sample.core.utils.toStateFlow
import me.aartikov.replica.advanced_sample.features.pokemons.createPokemonDetailsComponent
import me.aartikov.replica.advanced_sample.features.pokemons.createPokemonListComponent
import me.aartikov.replica.advanced_sample.features.pokemons.domain.PokemonId
import me.aartikov.replica.advanced_sample.features.pokemons.ui.list.PokemonListComponent

class RealPokemonsComponent(
    componentContext: ComponentContext,
    private val componentFactory: ComponentFactory
) : ComponentContext by componentContext, PokemonsComponent {

    private val navigation = StackNavigation<ChildConfig>()

    override val childStack: StateFlow<ChildStack<*, PokemonsComponent.Child>> = childStack(
        source = navigation,
        initialConfiguration = ChildConfig.List,
        handleBackButton = true,
        serializer = ChildConfig.serializer(),
        childFactory = ::createChild
    ).toStateFlow(lifecycle)

    private fun createChild(
        config: ChildConfig,
        componentContext: ComponentContext
    ): PokemonsComponent.Child = when (config) {
        is ChildConfig.List -> {
            PokemonsComponent.Child.List(
                componentFactory.createPokemonListComponent(
                    componentContext,
                    ::onPokemonListOutput
                )
            )
        }

        is ChildConfig.Details -> {
            PokemonsComponent.Child.Details(
                componentFactory.createPokemonDetailsComponent(
                    componentContext,
                    config.pokemonId
                )
            )
        }
    }

    private fun onPokemonListOutput(output: PokemonListComponent.Output) {
        when (output) {
            is PokemonListComponent.Output.PokemonDetailsRequested -> {
                navigation.pushNew(ChildConfig.Details(output.pokemonId))
            }
        }
    }

    @Serializable
    private sealed interface ChildConfig {

        @Serializable
        data object List : ChildConfig

        @Serializable
        data class Details(val pokemonId: PokemonId) : ChildConfig
    }
}