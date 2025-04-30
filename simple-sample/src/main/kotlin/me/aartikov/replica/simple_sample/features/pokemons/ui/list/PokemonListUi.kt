package me.aartikov.replica.simple_sample.features.pokemons.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import me.aartikov.replica.simple_sample.R
import me.aartikov.replica.simple_sample.core.theme.AppTheme
import me.aartikov.replica.simple_sample.core.utils.plus
import me.aartikov.replica.simple_sample.core.widget.ContentOrPlaceholder
import me.aartikov.replica.simple_sample.core.widget.EmptyPlaceholder
import me.aartikov.replica.simple_sample.core.widget.PullRefreshLceWidget
import me.aartikov.replica.simple_sample.core.widget.RefreshingProgress
import me.aartikov.replica.simple_sample.features.pokemons.domain.Pokemon
import me.aartikov.replica.simple_sample.features.pokemons.domain.PokemonId
import me.aartikov.replica.view_model.bindToLifecycle
import org.koin.androidx.compose.koinViewModel

@Composable
fun PokemonListUi(
    onPokemonClick: (PokemonId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PokemonListViewModel = koinViewModel<DefaultPokemonListViewModel>(),
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(lifecycle) {
        viewModel.bindToLifecycle(lifecycle)
    }

    val pokemonsState by viewModel.pokemonsState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PokemonTopBar()

        PullRefreshLceWidget(
            state = pokemonsState,
            onRefresh = viewModel::onRefresh,
            onRetryClick = viewModel::onRetryClick,
        ) { pokemons, refreshing, paddingValues ->
            ContentOrPlaceholder(
                items = pokemons,
                placeholder = {
                    EmptyPlaceholder(
                        modifier = Modifier.padding(paddingValues),
                        description = stringResource(R.string.pokemons_empty_description)
                    )
                }
            ) { list ->
                PokemonListContent(
                    pokemons = list,
                    onPokemonClick = onPokemonClick,
                    contentPadding = paddingValues,
                )
            }

            RefreshingProgress(
                modifier = Modifier.padding(top = 2.dp),
                active = refreshing
            )
        }
    }
}

@Composable
private fun PokemonTopBar(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onBackground,
        shadowElevation = 4.dp
    ) {
        Text(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}

@Composable
private fun PokemonListContent(
    pokemons: List<Pokemon>,
    onPokemonClick: (PokemonId) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding + PaddingValues(vertical = 12.dp)
    ) {
        items(
            items = pokemons,
            key = { pokemon -> pokemon.id.value }
        ) { pokemon ->
            PokemonItem(
                pokemon = pokemon,
                onClick = { onPokemonClick(pokemon.id) }
            )

            if (pokemon !== pokemons.lastOrNull()) HorizontalDivider()
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

@Preview
@Composable
private fun PokemonListPreview() {
    AppTheme {
        PokemonListUi(
            onPokemonClick = {},
            viewModel = PreviewPokemonListViewModel()
        )
    }
}
