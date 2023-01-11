package me.aartikov.replica.single

/**
 * Fetches data from a server. Throws exception on error.
 * @param T data type
 */
fun interface Fetcher<T : Any> {

    suspend fun fetch(): T
}