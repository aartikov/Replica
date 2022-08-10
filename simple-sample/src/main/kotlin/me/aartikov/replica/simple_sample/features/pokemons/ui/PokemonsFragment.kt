package me.aartikov.replica.simple_sample.features.pokemons.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import me.aartikov.replica.simple_sample.R
import me.aartikov.replica.simple_sample.features.pokemons.domain.PokemonId
import me.aartikov.replica.simple_sample.features.pokemons.ui.details.PokemonDetailsFragment
import me.aartikov.replica.simple_sample.features.pokemons.ui.list.PokemonListFragment
import me.aartikov.replica.simple_sample.features.pokemons.ui.list.PokemonsNavigation

class PokemonsFragment : Fragment(R.layout.fragment_pokemons), PokemonsNavigation {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            setupInitialFragment()
        }
    }

    private fun setupInitialFragment() {
        childFragmentManager.commit {
            add(R.id.container, PokemonListFragment())
        }
    }

    override fun navigateToDetails(pokemonId: PokemonId) {
        childFragmentManager.commit {
            replace(R.id.container, PokemonDetailsFragment.newInstance(pokemonId))
            addToBackStack("details")
        }
    }

    override fun navigateBack(): Boolean {
        return childFragmentManager.popBackStackImmediate()
    }
}