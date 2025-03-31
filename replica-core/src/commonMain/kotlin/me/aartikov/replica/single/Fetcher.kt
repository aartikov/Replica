package me.aartikov.replica.single

/**
 * Fetches data from a server.
 * Throws an exception on error.
 *
 * @param T The type of data to fetch.
 */
fun interface Fetcher<T : Any> {
    suspend fun fetch(): T
}