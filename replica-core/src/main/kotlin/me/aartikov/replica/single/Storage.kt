package me.aartikov.replica.single

/**
 * Allows [PhysicalReplica] to save data on a persistent storage.
 */
interface Storage<T : Any> {

    /**
     * Write [data] on a storage.
     */
    suspend fun write(data: T)

    /**
     * Read data from a storage.
     */
    suspend fun read(): T?

    /**
     * Remove data in a storage.
     */
    suspend fun remove()
}