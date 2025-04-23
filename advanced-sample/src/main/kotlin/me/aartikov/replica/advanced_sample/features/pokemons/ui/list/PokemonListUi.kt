package me.aartikov.replica.advanced_sample.features.pokemons.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import me.aartikov.replica.advanced_sample.R
import me.aartikov.replica.advanced_sample.core.theme.AppTheme
import me.aartikov.replica.advanced_sample.core.widget.EmptyPlaceholder
import me.aartikov.replica.advanced_sample.core.widget.PullRefreshLceWidget
import me.aartikov.replica.advanced_sample.core.widget.RefreshingProgress
import me.aartikov.replica.advanced_sample.features.pokemons.domain.Pokemon
import me.aartikov.replica.advanced_sample.features.pokemons.domain.PokemonId
import me.aartikov.replica.advanced_sample.features.pokemons.domain.PokemonType
import me.aartikov.replica.advanced_sample.features.pokemons.domain.PokemonTypeId
import me.aartikov.replica.single.Loadable

@Composable
fun PokemonListUi(
    component: PokemonListComponent,
    modifier: Modifier = Modifier,
) {
    val pokemonsState by component.pokemonsState.collectAsState()
    val selectedTypeId by component.selectedTypeId.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PokemonTypesRow(
                types = component.types,
                selectedTypeId = selectedTypeId,
                onTypeClick = component::onTypeClick
            )

            PullRefreshLceWidget(
                state = pokemonsState,
                onRefresh = component::onRefresh,
                onRetryClick = component::onRetryClick
            ) { pokemons, refreshing ->
                if (pokemons.isNotEmpty()) {
                    PokemonListContent(
                        pokemons = pokemons,
                        onPokemonClick = component::onPokemonClick
                    )
                } else {
                    EmptyPlaceholder(
                        modifier = Modifier.navigationBarsPadding(),
                        description = stringResource(R.string.pokemons_empty_description)
                    )
                }

                RefreshingProgress(refreshing)
            }
        }
    }
}

@Composable
private fun PokemonTypesRow(
    types: List<PokemonType>,
    selectedTypeId: PokemonTypeId,
    onTypeClick: (PokemonTypeId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 4.dp
    ) {
        Column(Modifier.statusBarsPadding()) {
            Text(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp),
                text = stringResource(R.string.pokemons_select_type),
                style = MaterialTheme.typography.titleLarge
            )
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                types.forEach {
                    PokemonTypeItem(
                        type = it,
                        isSelected = it.id == selectedTypeId,
                        onClick = { onTypeClick(it.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PokemonListContent(
    pokemons: List<Pokemon>,
    onPokemonClick: (PokemonId) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        items(
            items = pokemons,
            key = { pokemon -> pokemon.id.value }
        ) { pokemon ->
            PokemonItem(
                pokemon = pokemon,
                onClick = { onPokemonClick(pokemon.id) }
            )

            if (pokemon !== pokemons.lastOrNull()) {
                HorizontalDivider()
            }
        }

        item {
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@Composable
private fun PokemonItem(
    pokemon: Pokemon,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth(),
        text = pokemon.name
    )
}

@Preview(showSystemUi = true)
@Composable
private fun PokemonListUiPreview() {
    AppTheme {
        PokemonListUi(FakePokemonListComponent())
    }
}


class FakePokemonListComponent : PokemonListComponent {

    override val types = listOf(
        PokemonType.Fire,
        PokemonType.Water,
        PokemonType.Electric,
        PokemonType.Grass,
        PokemonType.Poison
    )

    override val selectedTypeId = MutableStateFlow(types[0].id)

    override val pokemonsState = MutableStateFlow(
        Loadable(
            loading = true,
            data = listOf(
                Pokemon(
                    id = PokemonId("1"),
                    name = "Bulbasaur"
                ),
                Pokemon(
                    id = PokemonId("5"),
                    name = "Charmeleon"
                ),
                Pokemon(
                    id = PokemonId("7"),
                    name = "Squirtle"
                )
            )
        )
    )

    override fun onTypeClick(typeId: PokemonTypeId) = Unit

    override fun onPokemonClick(pokemonId: PokemonId) = Unit

    override fun onRefresh() = Unit

    override fun onRetryClick() = Unit
}
