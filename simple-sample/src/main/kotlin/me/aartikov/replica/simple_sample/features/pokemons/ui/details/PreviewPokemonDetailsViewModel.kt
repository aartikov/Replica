package me.aartikov.replica.simple_sample.features.pokemons.ui.details

import kotlinx.coroutines.flow.MutableStateFlow
import me.aartikov.replica.simple_sample.features.pokemons.domain.DetailedPokemon
import me.aartikov.replica.single.Loadable

class PreviewPokemonDetailsViewModel : PokemonDetailsViewModel {

    override val activeFlow = MutableStateFlow(false)

    override val pokemonState = MutableStateFlow(
        Loadable<DetailedPokemon>(data = DetailedPokemon.MOCK)
    )

    override fun onPokemonImageClick(name: String) = Unit

    override fun onRefresh() = Unit

    override fun onRetryClick() = Unit
}
