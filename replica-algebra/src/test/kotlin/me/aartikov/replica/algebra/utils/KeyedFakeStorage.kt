package me.aartikov.replica.algebra.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.aartikov.replica.keyed.KeyedStorage

class KeyedFakeStorage : KeyedStorage<Int, String> {

    private val dataState = MutableStateFlow(emptyMap<Int, String>())

    override suspend fun write(key: Int, data: String) {
        dataState.update { it + (key to data) }
    }

    override suspend fun read(key: Int): String? {
        return dataState.value[key]
    }

    override suspend fun remove(key: Int) {
        dataState.update { it - key }
    }

    override suspend fun removeAll() {
        dataState.update { emptyMap() }
    }
}