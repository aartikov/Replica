package me.aartikov.replica.keyed

interface KeyedStorage<K : Any, T : Any> {

    suspend fun write(key: K, data: T)

    suspend fun read(key: K): T?

    suspend fun remove(key: K)

    suspend fun removeAll()
}