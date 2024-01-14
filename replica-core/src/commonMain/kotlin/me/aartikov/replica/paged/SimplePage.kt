package me.aartikov.replica.paged

data class SimplePage<out I : Any>(
    override val items: List<I>,
    override val hasNextPage: Boolean,
    override val hasPreviousPage: Boolean = false
) : Page<I>