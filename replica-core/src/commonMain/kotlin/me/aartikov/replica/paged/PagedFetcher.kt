package me.aartikov.replica.paged

interface PagedFetcher<I : Any, P : Page<I>> {

    suspend fun fetchFirstPage(): P

    suspend fun fetchNextPage(currentData: PagedData<I, P>): P

    suspend fun fetchPreviousPage(currentData: PagedData<I, P>): P {
        throw UnsupportedOperationException()
    }
}