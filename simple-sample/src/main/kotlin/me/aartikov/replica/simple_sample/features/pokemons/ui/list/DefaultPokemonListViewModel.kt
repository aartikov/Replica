package me.aartikov.replica.simple_sample.features.pokemons.ui.list

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import me.aartikov.replica.simple_sample.core.error_handling.ErrorHandler
import me.aartikov.replica.simple_sample.core.utils.observe
import me.aartikov.replica.simple_sample.features.pokemons.data.PokemonRepository

class DefaultPokemonListViewModel(
    pokemonRepository: PokemonRepository,
    errorHandler: ErrorHandler,
) : PokemonListViewModel, ViewModel() {

    override val activeFlow = MutableStateFlow(false)

    private val pokemonsReplica = pokemonRepository.pokemonsReplica

    override val pokemonsState = pokemonsReplica.observe(this, errorHandler)

    override fun onRefresh() {
        pokemonsReplica.refresh()
    }

    override fun onRetryClick() {
        pokemonsReplica.refresh()
    }
}
