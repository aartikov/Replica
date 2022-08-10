package me.aartikov.replica.simple_sample.features.pokemons.ui.details

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import me.aartikov.replica.simple_sample.R
import me.aartikov.replica.simple_sample.core.utils.SwipeRefreshLceController
import me.aartikov.replica.simple_sample.databinding.FragmentPokemonDetailsBinding
import me.aartikov.replica.simple_sample.features.pokemons.domain.DetailedPokemon
import me.aartikov.replica.simple_sample.features.pokemons.domain.PokemonId
import me.aartikov.replica.view_model.bindToLifecycle
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class PokemonDetailsFragment : Fragment(R.layout.fragment_pokemon_details) {

    companion object {
        private const val ARG_POKEMON_ID = "ARG_POKEMON_ID"

        fun newInstance(pokemonId: PokemonId): PokemonDetailsFragment {
            return PokemonDetailsFragment().apply {
                arguments = bundleOf(ARG_POKEMON_ID to pokemonId)
            }
        }
    }

    private val vm by viewModel<PokemonDetailsViewModel> {
        parametersOf(requireArguments().get(ARG_POKEMON_ID))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.bindToLifecycle(viewLifecycleOwner.lifecycle)
        FragmentPokemonDetailsBinding.bind(view).setupViews()
    }

    private fun FragmentPokemonDetailsBinding.setupViews() {
        swipeRefresh.setColorSchemeResources(R.color.mint_dark)

        val lceController = SwipeRefreshLceController<DetailedPokemon>(
            swipeRefeshView = swipeRefresh,
            loadingView = loadingView,
            errorView = errorView,
            setContent = { pokemon, refreshing ->
                name.text = pokemon.name
                image.load(pokemon.imageUrl) {
                    crossfade(true)
                    placeholder(R.drawable.bg_pokemon_image)
                    transformations(CircleCropTransformation())
                }
                height.text = getString(R.string.pokemons_height, pokemon.height)
                weight.text = getString(R.string.pokemons_weight, pokemon.weight)
                refreshingView.root.isVisible = refreshing
            },
            onRefresh = { vm.onRefresh() },
            onRetryClick = { vm.onRetryClick() }
        )

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.pokemonState.collect { lceController.setState(it) }
        }
    }
}