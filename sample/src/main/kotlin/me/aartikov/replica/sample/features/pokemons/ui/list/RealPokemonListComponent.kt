package me.aartikov.replica.sample.features.pokemons.ui.list

import android.os.Parcelable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.arkivanov.decompose.ComponentContext
import kotlinx.parcelize.Parcelize
import me.aartikov.replica.keyed.KeyedReplica
import me.aartikov.replica.keyed.keepPreviousData
import me.aartikov.replica.sample.core.error_handling.ErrorHandler
import me.aartikov.replica.sample.core.utils.observe
import me.aartikov.replica.sample.core.utils.persistent
import me.aartikov.replica.sample.features.pokemons.domain.Pokemon
import me.aartikov.replica.sample.features.pokemons.domain.PokemonId
import me.aartikov.replica.sample.features.pokemons.domain.PokemonType
import me.aartikov.replica.sample.features.pokemons.domain.PokemonTypeId

class RealPokemonListComponent(
    componentContext: ComponentContext,
    private val onOutput: (PokemonListComponent.Output) -> Unit,
    private val pokemonsByTypeReplica: KeyedReplica<PokemonTypeId, List<Pokemon>>,
    errorHandler: ErrorHandler
) : ComponentContext by componentContext, PokemonListComponent {

    override val types = listOf(
        PokemonType.Fire,
        PokemonType.Water,
        PokemonType.Electric,
        PokemonType.Grass,
        PokemonType.Poison
    )

    override var selectedTypeId by mutableStateOf(types[0].id)
        private set

    override val pokemonsState by pokemonsByTypeReplica
        .keepPreviousData() // for better UX
        .observe(
            lifecycle,
            errorHandler,
            key = { selectedTypeId }
        )

    init {
        persistent(
            save = { PersistentState(selectedTypeId) },
            restore = { state -> selectedTypeId = state.selectedTypeId }
        )
    }

    override fun onTypeClick(typeId: PokemonTypeId) {
        selectedTypeId = typeId
    }

    override fun onPokemonClick(pokemonId: PokemonId) {
        onOutput(PokemonListComponent.Output.PokemonDetailsRequested(pokemonId))
    }

    override fun onRefresh() {
        pokemonsByTypeReplica.refresh(selectedTypeId)
    }

    override fun onRetryClick() {
        pokemonsByTypeReplica.refresh(selectedTypeId)
    }

    @Parcelize
    private data class PersistentState(
        val selectedTypeId: PokemonTypeId
    ) : Parcelable
}