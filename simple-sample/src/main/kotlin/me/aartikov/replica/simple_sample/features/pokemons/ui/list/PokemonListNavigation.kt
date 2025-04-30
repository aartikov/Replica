package me.aartikov.replica.simple_sample.features.pokemons.ui.list

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import me.aartikov.replica.simple_sample.features.pokemons.domain.PokemonId

@Serializable
data object PokemonList

fun NavGraphBuilder.pokemonListScreen(
    onNavigateToPokemonDetails: (PokemonId) -> Unit,
) {
    composable<PokemonList> { backStackEntry ->
        PokemonListUi(
            onPokemonClick = onNavigateToPokemonDetails
        )
    }
}
