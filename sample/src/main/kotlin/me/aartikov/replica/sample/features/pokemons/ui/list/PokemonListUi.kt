package me.aartikov.replica.sample.features.pokemons.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.aartikov.replica.sample.R
import me.aartikov.replica.sample.core.theme.AppTheme
import me.aartikov.replica.sample.core.widget.EmptyPlaceholder
import me.aartikov.replica.sample.core.widget.RefreshingProgress
import me.aartikov.replica.sample.core.widget.SwipeRefreshLceWidget
import me.aartikov.replica.sample.features.pokemons.domain.Pokemon
import me.aartikov.replica.sample.features.pokemons.domain.PokemonId
import me.aartikov.replica.sample.features.pokemons.domain.PokemonType
import me.aartikov.replica.sample.features.pokemons.domain.PokemonTypeId
import me.aartikov.replica.single.Loadable

@Composable
fun PokemonListUi(
    component: PokemonListComponent,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PokemonTypesRow(
                types = component.types,
                selectedTypeId = component.selectedTypeId,
                onTypeClick = component::onTypeClick
            )

            SwipeRefreshLceWidget(
                state = component.pokemonsState,
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
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colors.background,
        elevation = 4.dp
    ) {
        Column {
            Text(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp),
                text = stringResource(R.string.pokemons_select_type),
                style = MaterialTheme.typography.h6
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
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        items(
            items = pokemons,
            key = { pokemon -> pokemon.id }
        ) { pokemon ->
            PokemonItem(
                pokemon = pokemon,
                onClick = { onPokemonClick(pokemon.id) }
            )

            if (pokemon !== pokemons.lastOrNull()) {
                Divider()
            }
        }
    }
}

@Composable
private fun PokemonItem(
    pokemon: Pokemon,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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
fun PokemonListUiPreview() {
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

    override val selectedTypeId = types[0].id

    override val pokemonsState = Loadable(
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

    override fun onTypeClick(typeId: PokemonTypeId) = Unit

    override fun onPokemonClick(pokemonId: PokemonId) = Unit

    override fun onRefresh() = Unit

    override fun onRetryClick() = Unit
}
