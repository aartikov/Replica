package me.aartikov.replica.keyed

/**
 * Allows [KeyedPhysicalReplica] to save data on a persistent storage.
 */
interface KeyedStorage<K : Any, T : Any> {

    /**
     * Write [data] on a storage for a given [key].
     */
    suspend fun write(key: K, data: T)

    /**
     * Read data from a storage for a given [key].
     */
    suspend fun read(key: K): T?

    /**
     * Remove data in a storage for a given [key].
     */
    suspend fun remove(key: K)

    /**
     * Remove all data in a storage.
     */
    suspend fun removeAll()
}