package me.aartikov.replica.simple_sample.features.pokemons.ui.list

import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.simple_sample.features.pokemons.domain.Pokemon
import me.aartikov.replica.single.Loadable
import me.aartikov.replica.view_model.Activable

interface PokemonListViewModel : Activable {

    val pokemonsState: StateFlow<Loadable<List<Pokemon>>>

    fun onRefresh()

    fun onRetryClick()
}
