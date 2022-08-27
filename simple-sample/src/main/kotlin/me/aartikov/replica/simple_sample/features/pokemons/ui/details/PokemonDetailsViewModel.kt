package me.aartikov.replica.simple_sample.features.pokemons.ui.details

import androidx.lifecycle.ViewModel
import me.aartikov.replica.simple_sample.core.error_handling.ErrorHandler
import me.aartikov.replica.simple_sample.core.utils.observe
import me.aartikov.replica.simple_sample.features.pokemons.domain.DetailedPokemon
import me.aartikov.replica.single.Replica
import me.aartikov.replica.view_model.Activable
import me.aartikov.replica.view_model.activable

class PokemonDetailsViewModel(
    private val pokemonReplica: Replica<DetailedPokemon>,
    errorHandler: ErrorHandler
) : ViewModel(), Activable by activable() {

    val pokemonState = pokemonReplica.observe(this, errorHandler)

    fun onRefresh() {
        pokemonReplica.refresh()
    }

    fun onRetryClick() {
        pokemonReplica.refresh()
    }
}