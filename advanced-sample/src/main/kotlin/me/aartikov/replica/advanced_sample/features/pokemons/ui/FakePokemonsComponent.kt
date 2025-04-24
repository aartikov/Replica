package me.aartikov.replica.advanced_sample.features.pokemons.ui

import me.aartikov.replica.advanced_sample.core.utils.createFakeChildStackStateFlow
import me.aartikov.replica.advanced_sample.features.pokemons.ui.list.FakePokemonListComponent

class FakePokemonsComponent : PokemonsComponent {

    override val childStack = createFakeChildStackStateFlow(
        PokemonsComponent.Child.List(FakePokemonListComponent())
    )
}