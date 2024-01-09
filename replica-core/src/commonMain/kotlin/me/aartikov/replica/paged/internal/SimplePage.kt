package me.aartikov.replica.paged.internal

import me.aartikov.replica.paged.Page

internal data class SimplePage<out T : Any>(
    override val items: List<T>,
    override val hasNextPage: Boolean,
    override val hasPreviousPage: Boolean
) : Page<T>