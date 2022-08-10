package me.aartikov.replica.simple_sample.features.pokemons.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@JvmInline
value class PokemonId(val value: String) : Parcelable

data class Pokemon(
    val id: PokemonId,
    val name: String
)