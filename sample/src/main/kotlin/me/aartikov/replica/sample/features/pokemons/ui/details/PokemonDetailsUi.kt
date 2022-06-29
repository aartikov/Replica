package me.aartikov.replica.sample.features.pokemons.ui.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import me.aartikov.replica.sample.R
import me.aartikov.replica.sample.core.ui.theme.AppTheme
import me.aartikov.replica.sample.core.ui.widget.RefreshingProgress
import me.aartikov.replica.sample.core.ui.widget.SwipeRefreshLceWidget
import me.aartikov.replica.sample.features.pokemons.domain.DetailedPokemon
import me.aartikov.replica.sample.features.pokemons.domain.PokemonId
import me.aartikov.replica.sample.features.pokemons.domain.PokemonType
import me.aartikov.replica.sample.features.pokemons.ui.list.PokemonTypeItem
import me.aartikov.replica.single.Loadable

@Composable
fun PokemonDetailsUi(
    component: PokemonDetailsComponent,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        SwipeRefreshLceWidget(
            state = component.pokemonState,
            onRefresh = component::onRefresh,
            onRetryClick = component::onRetryClick
        ) { pokemon, refreshing ->
            PokemonDetailsContent(
                pokemon = pokemon
            )

            RefreshingProgress(
                refreshing,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun PokemonDetailsContent(
    pokemon: DetailedPokemon,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            textAlign = TextAlign.Center,
            text = pokemon.name,
            style = MaterialTheme.typography.h5
        )

        Image(
            painter = rememberImagePainter(pokemon.imageUrl) {
                crossfade(true)
            },
            contentDescription = null,
            modifier = Modifier
                .padding(top = 32.dp)
                .size(200.dp)
                .clip(CircleShape)
                .background(color = MaterialTheme.colors.surface)
        )

        Text(
            modifier = Modifier.padding(top = 32.dp),
            text = stringResource(R.string.pokemons_types)
        )

        Row(
            modifier = Modifier.padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            pokemon.types.forEach {
                PokemonTypeItem(type = it, isSelected = true)
            }
        }

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
fun PokemonDetailsUiPreview() {
    AppTheme {
        PokemonDetailsUi(FakePokemonDetailsComponent())
    }
}


class FakePokemonDetailsComponent : PokemonDetailsComponent {

    override val pokemonState = Loadable(
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

    override fun onRefresh() = Unit

    override fun onRetryClick() = Unit

}
