package me.aartikov.replica.simple_sample.features.pokemons.ui.list

import kotlinx.coroutines.flow.MutableStateFlow
import me.aartikov.replica.simple_sample.features.pokemons.domain.Pokemon
import me.aartikov.replica.single.Loadable

class PreviewPokemonListViewModel : PokemonListViewModel {

    override val activeFlow = MutableStateFlow(false)

    override val pokemonsState = MutableStateFlow(Loadable<List<Pokemon>>(data = Pokemon.MOCKS))

    override fun onRefresh() = Unit

    override fun onRetryClick() = Unit
}
