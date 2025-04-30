package me.aartikov.replica.simple_sample.app.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import me.aartikov.replica.simple_sample.features.pokemons.ui.details.PokemonDetails
import me.aartikov.replica.simple_sample.features.pokemons.ui.details.pokemonDetailsScreen
import me.aartikov.replica.simple_sample.features.pokemons.ui.list.PokemonList
import me.aartikov.replica.simple_sample.features.pokemons.ui.list.pokemonListScreen

@Composable
internal fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = PokemonList,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }
    ) {
        pokemonListScreen(
            onNavigateToPokemonDetails = { pokemonId ->
                navController.navigate(PokemonDetails(pokemonId))
            }
        )
        pokemonDetailsScreen()
    }
}
