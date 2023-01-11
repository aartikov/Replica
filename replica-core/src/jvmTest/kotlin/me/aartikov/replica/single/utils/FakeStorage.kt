package me.aartikov.replica.single.utils

import kotlinx.coroutines.flow.MutableStateFlow
import me.aartikov.replica.single.Storage

class FakeStorage : Storage<String> {

    private var dataState = MutableStateFlow<String?>(null)

    override suspend fun write(data: String) {
        dataState.value = data
    }

    override suspend fun read(): String? {
        return dataState.value
    }

    override suspend fun remove() {
        dataState.value = null
    }
}