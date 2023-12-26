package me.aartikov.replica.paged

interface PagedFetcher<T : Any, P : Page<T>> {

    suspend fun fetchFirstPage(): P

    suspend fun fetchNextPage(currentData: PagedData<T, P>): P

    suspend fun fetchPreviousPage(currentData: PagedData<T, P>): P {
        throw UnsupportedOperationException()
    }
}