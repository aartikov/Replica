package me.aartikov.replica.single.internal

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.aartikov.replica.single.Storage

// Storage that executes operations sequentially to prevent race conditions.
internal class SequentialStorage<T : Any>(
    private val originalStorage: Storage<T>,
    private val additionalMutex: Mutex? = null
) : Storage<T> {

    private val mutex: Mutex = Mutex()

    override suspend fun write(data: T) = withLock {
        originalStorage.write(data)
    }

    override suspend fun read(): T? = withLock {
        originalStorage.read()
    }

    override suspend fun remove() = withLock {
        originalStorage.remove()
    }

    private suspend inline fun <R> withLock(block: () -> R) = mutex.withLock {
        if (additionalMutex != null) {
            additionalMutex.withLock {
                block()
            }
        } else {
            block()
        }
    }
}