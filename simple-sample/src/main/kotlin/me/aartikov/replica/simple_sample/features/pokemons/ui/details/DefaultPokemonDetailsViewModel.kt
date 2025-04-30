package me.aartikov.replica.simple_sample.features.pokemons.ui.details

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import me.aartikov.replica.algebra.normal.withKey
import me.aartikov.replica.simple_sample.core.error_handling.ErrorHandler
import me.aartikov.replica.simple_sample.core.message.data.MessageService
import me.aartikov.replica.simple_sample.core.message.domain.Message
import me.aartikov.replica.simple_sample.core.utils.observe
import me.aartikov.replica.simple_sample.features.pokemons.data.PokemonRepository
import me.aartikov.replica.simple_sample.features.pokemons.domain.PokemonId
import me.aartikov.sesame.localizedstring.LocalizedString

class DefaultPokemonDetailsViewModel(
    pokemonId: PokemonId,
    pokemonRepository: PokemonRepository,
    errorHandler: ErrorHandler,
    private val messageService: MessageService,
) : PokemonDetailsViewModel, ViewModel() {

    override val activeFlow = MutableStateFlow(false)

    private val pokemonReplica = pokemonRepository.pokemonByIdReplica.withKey(pokemonId)

    override val pokemonState = pokemonReplica.observe(this, errorHandler)

    override fun onPokemonImageClick(name: String) {
        messageService.showMessage(
            Message(LocalizedString.raw(name))
        )
    }

    override fun onRefresh() {
        pokemonReplica.refresh()
    }

    override fun onRetryClick() {
        pokemonReplica.refresh()
    }
}
