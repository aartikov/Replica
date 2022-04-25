package me.aartikov.replica.single

/**
 * Allows [PhysicalReplica] to save data in a persistent storage.
 */
interface Storage<T : Any> {

    suspend fun write(data: T)

    suspend fun read(): T?

    suspend fun remove()
}