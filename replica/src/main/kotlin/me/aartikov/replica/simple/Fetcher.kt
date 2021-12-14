package me.aartikov.replica.simple

fun interface Fetcher<T : Any> {

    suspend fun fetch(): T
}