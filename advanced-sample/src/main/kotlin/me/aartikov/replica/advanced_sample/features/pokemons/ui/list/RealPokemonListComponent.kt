package me.aartikov.replica.advanced_sample.features.pokemons.ui.list

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import me.aartikov.replica.advanced_sample.core.error_handling.ErrorHandler
import me.aartikov.replica.advanced_sample.core.utils.observe
import me.aartikov.replica.advanced_sample.core.utils.persistent
import me.aartikov.replica.advanced_sample.features.pokemons.data.PokemonRepository
import me.aartikov.replica.advanced_sample.features.pokemons.domain.PokemonId
import me.aartikov.replica.advanced_sample.features.pokemons.domain.PokemonType
import me.aartikov.replica.advanced_sample.features.pokemons.domain.PokemonTypeId
import me.aartikov.replica.algebra.normal.withKey
import me.aartikov.replica.keyed.keepPreviousData

class RealPokemonListComponent(
    componentContext: ComponentContext,
    private val onOutput: (PokemonListComponent.Output) -> Unit,
    pokemonsRepository: PokemonRepository,
    errorHandler: ErrorHandler,
) : ComponentContext by componentContext, PokemonListComponent {

    override val types = listOf(
        PokemonType.Fire,
        PokemonType.Water,
        PokemonType.Electric,
        PokemonType.Grass,
        PokemonType.Poison
    )

    override val selectedTypeId = MutableStateFlow(types[0].id)

    private val pokemonsReplica =
        pokemonsRepository.pokemonsByTypeReplica.keepPreviousData().withKey(selectedTypeId)

    override val pokemonsState = pokemonsReplica.observe(lifecycle, errorHandler)

    init {
        persistent(
            save = { PersistentState(selectedTypeId.value) },
            restore = { state -> selectedTypeId.value = state.selectedTypeId },
            serializer = PersistentState.serializer()
        )
    }

    override fun onTypeClick(typeId: PokemonTypeId) {
        selectedTypeId.value = typeId
    }

    override fun onPokemonClick(pokemonId: PokemonId) {
        onOutput(PokemonListComponent.Output.PokemonDetailsRequested(pokemonId))
    }

    override fun onRefresh() {
        pokemonsReplica.refresh()
    }

    override fun onRetryClick() {
        pokemonsReplica.refresh()
    }

    @Serializable
    private data class PersistentState(
        val selectedTypeId: PokemonTypeId,
    )
}