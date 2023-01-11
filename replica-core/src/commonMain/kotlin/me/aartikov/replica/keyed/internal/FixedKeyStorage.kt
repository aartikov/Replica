package me.aartikov.replica.keyed.internal

import me.aartikov.replica.keyed.KeyedStorage
import me.aartikov.replica.single.Storage

internal class FixedKeyStorage<K : Any, T : Any>(
    private val keyedStorage: KeyedStorage<K, T>,
    private val key: K
) : Storage<T> {

    override suspend fun write(data: T) {
        keyedStorage.write(key, data)
    }

    override suspend fun read(): T? {
        return keyedStorage.read(key)
    }

    override suspend fun remove() {
        keyedStorage.remove(key)
    }
}