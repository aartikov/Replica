package me.aartikov.replica.keyed

fun interface KeyedFetcher<K : Any, T : Any> {

    suspend fun fetch(key: K): T
}