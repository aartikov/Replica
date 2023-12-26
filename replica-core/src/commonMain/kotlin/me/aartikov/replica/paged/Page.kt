package me.aartikov.replica.paged

interface Page<out T : Any> {

    val data: List<T>

    val hasNextPage: Boolean

    val hasPreviousPage: Boolean
}