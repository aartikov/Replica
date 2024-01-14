package me.aartikov.replica.paged.internal

import me.aartikov.replica.paged.Page

internal data class SimplePage<out I : Any>(
    override val items: List<I>,
    override val hasNextPage: Boolean,
    override val hasPreviousPage: Boolean
) : Page<I>