package me.aartikov.replica.advanced_sample.features.pokemons.ui.details

import com.arkivanov.decompose.ComponentContext
import me.aartikov.replica.advanced_sample.core.error_handling.ErrorHandler
import me.aartikov.replica.advanced_sample.core.message.data.MessageService
import me.aartikov.replica.advanced_sample.core.message.domain.Message
import me.aartikov.replica.advanced_sample.core.utils.observe
import me.aartikov.replica.advanced_sample.features.pokemons.data.PokemonRepository
import me.aartikov.replica.advanced_sample.features.pokemons.domain.PokemonId
import me.aartikov.replica.advanced_sample.features.pokemons.domain.PokemonType
import me.aartikov.replica.algebra.normal.withKey
import me.aartikov.sesame.localizedstring.LocalizedString

class RealPokemonDetailsComponent(
    componentContext: ComponentContext,
    pokemonId: PokemonId,
    pokemonRepository: PokemonRepository,
    errorHandler: ErrorHandler,
    private val messageService: MessageService,
) : ComponentContext by componentContext, PokemonDetailsComponent {

    private val pokemonReplica = pokemonRepository.pokemonByIdReplica.withKey(pokemonId)

    override val pokemonState = pokemonReplica.observe(lifecycle, errorHandler)

    override fun onTypeClick(type: PokemonType) {
        pokemonState.value.data?.let {
            messageService.showMessage(
                Message(
                    text = LocalizedString.raw("Types are: ${it.types.joinToString { it.name }}")
                )
            )
        }
    }

    override fun onRefresh() {
        pokemonReplica.refresh()
    }

    override fun onRetryClick() {
        pokemonReplica.refresh()
    }
}