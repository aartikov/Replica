package me.aartikov.replica.paged

import me.aartikov.replica.paged.internal.SimplePage

interface Page<out I : Any> {

    val items: List<I>

    val hasNextPage: Boolean

    val hasPreviousPage: Boolean
}

fun <I : Any> Page(
    items: List<I>,
    hasNextPage: Boolean,
    hasPreviousPage: Boolean = false
): Page<I> {
    return SimplePage(items, hasNextPage, hasPreviousPage)
}