package me.aartikov.replica.single

fun interface Fetcher<T : Any> {

    suspend fun fetch(): T
}