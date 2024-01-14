package me.aartikov.replica.paged

interface Page<out I : Any> {

    val items: List<I>

    val hasNextPage: Boolean

    val hasPreviousPage: Boolean
}