package me.aartikov.replica.simple_sample.features.pokemons.ui.details

import android.os.Bundle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import me.aartikov.replica.simple_sample.features.pokemons.domain.PokemonId
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.reflect.typeOf

@Serializable
data class PokemonDetails(val id: PokemonId)

private object PokemonIdNavType : NavType<PokemonId>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): PokemonId =
        bundle.getString(key)?.let(::parseValue) ?: error("Missing PokemonId")

    override fun put(bundle: Bundle, key: String, value: PokemonId) =
        bundle.putString(key, value.value)

    override fun parseValue(value: String): PokemonId = PokemonId(value)

    override fun serializeAsValue(value: PokemonId): String = value.value
}

fun NavGraphBuilder.pokemonDetailsScreen() {
    composable<PokemonDetails>(
        typeMap = mapOf(typeOf<PokemonId>() to PokemonIdNavType)
    ) { backStackEntry ->
        val pokemonDetails = backStackEntry.toRoute<PokemonDetails>()

        PokemonDetailsUi(
            viewModel = koinViewModel<DefaultPokemonDetailsViewModel> {
                parametersOf(pokemonDetails.id)
            }
        )
    }
}
