package me.aartikov.replica.sample.features.pokemons.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.arkivanov.decompose.extensions.compose.jetpack.Children
import me.aartikov.replica.sample.core.ui.theme.AppTheme
import me.aartikov.replica.sample.core.ui.utils.createFakeRouterState
import me.aartikov.replica.sample.features.pokemons.ui.details.PokemonDetailsUi
import me.aartikov.replica.sample.features.pokemons.ui.list.FakePokemonListComponent
import me.aartikov.replica.sample.features.pokemons.ui.list.PokemonListUi

@Composable
fun PokemonsUi(
    component: PokemonsComponent,
    modifier: Modifier = Modifier
) {
    Children(component.routerState) { child ->
        when (val instance = child.instance) {
            is PokemonsComponent.Child.List -> PokemonListUi(instance.component, modifier)
            is PokemonsComponent.Child.Details -> PokemonDetailsUi(instance.component, modifier)
        }
    }
}

@Preview
@Composable
fun PokemonsUiPreview() {
    AppTheme {
        PokemonsUi(FakePokemonsComponent())
    }
}


class FakePokemonsComponent : PokemonsComponent {

    override val routerState = createFakeRouterState(
        PokemonsComponent.Child.List(FakePokemonListComponent())
    )
}
