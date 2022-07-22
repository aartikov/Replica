package me.aartikov.replica.sample.features.pokemons.ui

import android.os.Parcelable
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.RouterState
import com.arkivanov.decompose.router.push
import com.arkivanov.decompose.router.router
import kotlinx.parcelize.Parcelize
import me.aartikov.replica.sample.core.ComponentFactory
import me.aartikov.replica.sample.core.utils.toComposeState
import me.aartikov.replica.sample.features.pokemons.createPokemonDetailsComponent
import me.aartikov.replica.sample.features.pokemons.createPokemonListComponent
import me.aartikov.replica.sample.features.pokemons.domain.PokemonId
import me.aartikov.replica.sample.features.pokemons.ui.list.PokemonListComponent

class RealPokemonsComponent(
    componentContext: ComponentContext,
    private val componentFactory: ComponentFactory
) : ComponentContext by componentContext, PokemonsComponent {

    private val router = router<ChildConfig, PokemonsComponent.Child>(
        initialConfiguration = ChildConfig.List,
        handleBackButton = true,
        childFactory = ::createChild
    )

    override val routerState: RouterState<*, PokemonsComponent.Child>
        by router.state.toComposeState(lifecycle)

    private fun createChild(
        config: ChildConfig,
        componentContext: ComponentContext
    ): PokemonsComponent.Child {
        return when (config) {
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
    }

    private fun onPokemonListOutput(output: PokemonListComponent.Output) {
        when (output) {
            is PokemonListComponent.Output.PokemonDetailsRequested -> {
                router.push(ChildConfig.Details(output.pokemonId))
            }
        }
    }

    private sealed interface ChildConfig : Parcelable {

        @Parcelize
        object List : ChildConfig

        @Parcelize
        data class Details(val pokemonId: PokemonId) : ChildConfig
    }
}