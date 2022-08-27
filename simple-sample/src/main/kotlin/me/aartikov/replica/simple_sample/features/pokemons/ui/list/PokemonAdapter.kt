package me.aartikov.replica.simple_sample.features.pokemons.ui.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import me.aartikov.replica.simple_sample.R
import me.aartikov.replica.simple_sample.features.pokemons.domain.Pokemon
import me.aartikov.replica.simple_sample.features.pokemons.domain.PokemonId

class PokemonAdapter(
    private val onPokemonClicked: (PokemonId) -> Unit
) : ListAdapter<Pokemon, PokemonAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_pokemon, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(getItem(position))
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        var currentPokemon: Pokemon? = null

        init {
            view.setOnClickListener {
                currentPokemon?.let {
                    onPokemonClicked(it.id)
                }
            }
        }

        fun bind(pokemon: Pokemon) {
            currentPokemon = pokemon
            name.text = pokemon.name
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Pokemon>() {
        override fun areItemsTheSame(oldItem: Pokemon, newItem: Pokemon): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Pokemon, newItem: Pokemon): Boolean {
            return oldItem == newItem
        }
    }
}
