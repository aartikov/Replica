package me.aartikov.replica.keyed_paged

import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedData

/**
 * Fetches paged data from a server.
 * @param K key type
 * @param I item type
 * @param P page type
 */
interface KeyedPagedFetcher<K : Any, I : Any, P : Page<I>> {

    suspend fun fetchFirstPage(key: K): P

    suspend fun fetchNextPage(key: K, currentData: PagedData<I, P>): P

    suspend fun fetchPreviousPage(key: K, currentData: PagedData<I, P>): P {
        throw UnsupportedOperationException()
    }
}