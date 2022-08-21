package me.aartikov.replica.simple_sample.features.pokemons.ui.list

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import kotlinx.coroutines.launch
import me.aartikov.replica.simple_sample.R
import me.aartikov.replica.simple_sample.core.utils.SwipeRefreshLceController
import me.aartikov.replica.simple_sample.databinding.FragmentPokemonListBinding
import me.aartikov.replica.simple_sample.features.pokemons.domain.Pokemon
import me.aartikov.replica.simple_sample.features.pokemons.domain.PokemonId
import me.aartikov.replica.view_model.bindToLifecycle
import org.koin.androidx.viewmodel.ext.android.viewModel

class PokemonListFragment : Fragment(R.layout.fragment_pokemon_list) {

    private val vm by viewModel<PokemonListViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.bindToLifecycle(viewLifecycleOwner.lifecycle)
        FragmentPokemonListBinding.bind(view).setupViews()
    }

    private fun FragmentPokemonListBinding.setupViews() {
        val adapter = PokemonAdapter(onPokemonClicked = ::navigateToDetails)
        pokemonRecycler.adapter = adapter
        pokemonRecycler.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )

        swipeRefresh.setColorSchemeResources(R.color.mint_dark)
        emptyView.message.text = getString(R.string.pokemons_empty_description)

        val lceController = SwipeRefreshLceController<List<Pokemon>>(
            swipeRefeshView = swipeRefresh,
            loadingView = loadingView,
            errorView = errorView,
            setContent = { pokemons, refreshing ->
                adapter.submitList(pokemons)
                refreshingView.root.isVisible = refreshing
                emptyView.root.isVisible = pokemons.isEmpty()
            },
            resetContent = {
                adapter.submitList(null)
            },
            onRefresh = { vm.onRefresh() },
            onRetryClick = { vm.onRetryClick() }
        )

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.pokemonsState.collect { lceController.setState(it) }
            }
        }
    }

    private fun navigateToDetails(pokemonId: PokemonId) {
        (parentFragment as PokemonsNavigation).navigateToDetails(pokemonId)
    }
}