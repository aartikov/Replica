package me.aartikov.replica.advanced_sample.features.pokemons.ui.details

import androidx.compose.runtime.getValue
import com.arkivanov.decompose.ComponentContext
import me.aartikov.replica.advanced_sample.core.error_handling.ErrorHandler
import me.aartikov.replica.advanced_sample.core.utils.observe
import me.aartikov.replica.advanced_sample.features.pokemons.domain.DetailedPokemon
import me.aartikov.replica.single.Replica

class RealPokemonDetailsComponent(
    componentContext: ComponentContext,
    private val pokemonReplica: Replica<DetailedPokemon>,
    errorHandler: ErrorHandler
) : ComponentContext by componentContext, PokemonDetailsComponent {

    override val pokemonState by pokemonReplica.observe(lifecycle, errorHandler)

    override fun onRefresh() {
        pokemonReplica.refresh()
    }

    override fun onRetryClick() {
        pokemonReplica.refresh()
    }
}