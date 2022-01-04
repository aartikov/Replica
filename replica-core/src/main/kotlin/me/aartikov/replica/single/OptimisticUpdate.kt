package me.aartikov.replica.single

fun interface OptimisticUpdate<T : Any> {
    fun apply(data: T): T
}