package me.aartikov.replica.paged

import me.aartikov.replica.paged.internal.SimplePage

interface Page<out T : Any> {

    val items: List<T>

    val hasNextPage: Boolean

    val hasPreviousPage: Boolean
}

fun <T : Any> Page(items: List<T>, hasNextPage: Boolean, hasPreviousPage: Boolean): Page<T> {
    return SimplePage(items, hasNextPage, hasPreviousPage)
}