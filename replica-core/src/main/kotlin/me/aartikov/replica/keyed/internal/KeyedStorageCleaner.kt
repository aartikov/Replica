package me.aartikov.replica.keyed.internal

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.aartikov.replica.keyed.KeyedStorage

internal class KeyedStorageCleaner<T : Any>(
    private val keyedStorage: KeyedStorage<*, T>
) {

    val mutex = Mutex()

    suspend fun removeAll() = mutex.withLock {
        keyedStorage.removeAll()
    }
}