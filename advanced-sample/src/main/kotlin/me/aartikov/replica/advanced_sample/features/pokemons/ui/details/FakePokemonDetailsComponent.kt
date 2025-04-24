package me.aartikov.replica.advanced_sample.features.pokemons.ui.details

import kotlinx.coroutines.flow.MutableStateFlow
import me.aartikov.replica.advanced_sample.features.pokemons.domain.DetailedPokemon
import me.aartikov.replica.advanced_sample.features.pokemons.domain.PokemonId
import me.aartikov.replica.advanced_sample.features.pokemons.domain.PokemonType
import me.aartikov.replica.single.Loadable

class FakePokemonDetailsComponent : PokemonDetailsComponent {

    override val pokemonState = MutableStateFlow(
        Loadable(
            loading = true,
            data = DetailedPokemon(
                id = PokemonId("1"),
                name = "Bulbasaur",
                imageUrl = "",
                height = 0.7f,
                weight = 6.9f,
                types = listOf(PokemonType.Grass, PokemonType.Poison)
            )
        )
    )

    override fun onTypeClick(type: PokemonType) = Unit

    override fun onRefresh() = Unit

    override fun onRetryClick() = Unit
}
