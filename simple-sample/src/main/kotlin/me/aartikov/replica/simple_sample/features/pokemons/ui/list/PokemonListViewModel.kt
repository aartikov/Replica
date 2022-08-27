package me.aartikov.replica.simple_sample.features.pokemons.ui.list

import androidx.lifecycle.ViewModel
import me.aartikov.replica.simple_sample.core.error_handling.ErrorHandler
import me.aartikov.replica.simple_sample.core.utils.observe
import me.aartikov.replica.simple_sample.features.pokemons.domain.Pokemon
import me.aartikov.replica.single.Replica
import me.aartikov.replica.view_model.Activable
import me.aartikov.replica.view_model.activable

class PokemonListViewModel(
    private val pokemonsReplica: Replica<List<Pokemon>>,
    errorHandler: ErrorHandler
) : ViewModel(), Activable by activable() {

    val pokemonsState = pokemonsReplica.observe(this, errorHandler)

    fun onRefresh() {
        pokemonsReplica.refresh()
    }

    fun onRetryClick() {
        pokemonsReplica.refresh()
    }
}