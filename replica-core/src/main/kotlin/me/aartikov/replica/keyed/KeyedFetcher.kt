package me.aartikov.replica.keyed

/**
 * Fetches data from a server. Throws exception on error.
 * @param K key type
 * @param T data type
 */
fun interface KeyedFetcher<K : Any, T : Any> {

    suspend fun fetch(key: K): T
}