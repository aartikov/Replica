package me.aartikov.replica.sample.features.pokemons.ui.details

import androidx.compose.runtime.getValue
import com.arkivanov.decompose.ComponentContext
import me.aartikov.replica.sample.core.ui.error_handing.ErrorHandler
import me.aartikov.replica.sample.core.ui.utils.observe
import me.aartikov.replica.sample.features.pokemons.domain.DetailedPokemon
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