package me.aartikov.replica.single

interface Storage<T : Any> {

    suspend fun write(data: T)

    suspend fun read(): T?

    suspend fun clear()
}