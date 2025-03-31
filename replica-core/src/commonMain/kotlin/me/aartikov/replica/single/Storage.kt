package me.aartikov.replica.single

/**
 * Allows [PhysicalReplica] to save data on a persistent storage.
 */
interface Storage<T : Any> {

    /**
     * Write [data] to a storage.
     */
    suspend fun write(data: T)

    /**
     * Read data from a storage.
     */
    suspend fun read(): T?

    /**
     * Remove data from a storage.
     */
    suspend fun remove()
}