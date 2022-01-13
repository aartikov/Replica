package me.aartikov.replica.sample.features.pokemons.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.aartikov.replica.keyed.KeyedStorage
import me.aartikov.replica.sample.features.pokemons.domain.DetailedPokemon
import me.aartikov.replica.sample.features.pokemons.domain.PokemonId

class PokemonStorage(context: Context) : KeyedStorage<PokemonId, DetailedPokemon> {

    private val preferences = context.getSharedPreferences("pokemons", Context.MODE_PRIVATE)

    override suspend fun write(key: PokemonId, data: DetailedPokemon) {
        preferences.edit {
            putString("pokemon_${key.value}", Json.encodeToString(data))
        }
    }

    override suspend fun read(key: PokemonId): DetailedPokemon? {
        val json = preferences.getString("pokemon_${key.value}", null) ?: return null
        return Json.decodeFromString(json)
    }

    override suspend fun remove(key: PokemonId) {
        preferences.edit {
            remove("pokemon_${key.value}")
        }
    }

    override suspend fun removeAll() {
        preferences.edit {
            clear()
        }
    }
}