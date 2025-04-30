package me.aartikov.replica.simple_sample.features.pokemons.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import coil.request.ImageRequest
import me.aartikov.replica.simple_sample.R
import me.aartikov.replica.simple_sample.core.theme.AppTheme
import me.aartikov.replica.simple_sample.core.widget.PullRefreshLceWidget
import me.aartikov.replica.simple_sample.core.widget.RefreshingProgress
import me.aartikov.replica.simple_sample.features.pokemons.domain.DetailedPokemon
import me.aartikov.replica.view_model.bindToLifecycle
import org.koin.androidx.compose.koinViewModel

@Composable
fun PokemonDetailsUi(
    modifier: Modifier = Modifier,
    viewModel: PokemonDetailsViewModel = koinViewModel<DefaultPokemonDetailsViewModel>(),
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(lifecycle) {
        viewModel.bindToLifecycle(lifecycle)
    }

    val pokemonState by viewModel.pokemonState.collectAsState()

    PullRefreshLceWidget(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        state = pokemonState,
        onRefresh = viewModel::onRefresh,
        onRetryClick = viewModel::onRetryClick,
        contentWindowInsets = WindowInsets.systemBars,
    ) { pokemon, refreshing, paddingValues ->
        PokemonDetailsContent(
            modifier = Modifier.fillMaxSize(),
            pokemon = pokemon,
            contentPadding = paddingValues,
            onImageClick = viewModel::onPokemonImageClick
        )

        RefreshingProgress(
            modifier = Modifier.padding(paddingValues),
            active = refreshing
        )
    }
}

@Composable
private fun PokemonDetailsContent(
    pokemon: DetailedPokemon,
    contentPadding: PaddingValues,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            textAlign = TextAlign.Center,
            text = pokemon.name,
            style = MaterialTheme.typography.headlineSmall
        )

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(pokemon.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 32.dp)
                .size(200.dp)
                .clip(CircleShape)
                .background(color = MaterialTheme.colorScheme.surface)
                .clickable { onImageClick(pokemon.name) }
        )

        Row(
            modifier = Modifier.padding(top = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.pokemons_height, pokemon.height)
            )
            Text(
                text = stringResource(R.string.pokemons_weight, pokemon.weight)
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun PokemonDetailsUiPreview() {
    AppTheme {
        PokemonDetailsUi(
            viewModel = PreviewPokemonDetailsViewModel()
        )
    }
}
