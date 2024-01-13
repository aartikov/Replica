package me.aartikov.replica.keyed_paged

import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedData

/**
 * Fetches paged data from a server.
 * @param K key type
 * @param T data type
 * @param P page type
 */
interface KeyedPagedFetcher<K : Any, T : Any, P : Page<T>> {

    suspend fun fetchFirstPage(key: K): P

    suspend fun fetchNextPage(key: K, currentData: PagedData<T, P>): P

    suspend fun fetchPreviousPage(key: K, currentData: PagedData<T, P>): P {
        throw UnsupportedOperationException()
    }
}