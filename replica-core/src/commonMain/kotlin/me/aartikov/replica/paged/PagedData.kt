package me.aartikov.replica.paged

data class PagedData<out I : Any, out P : Page<I>>(
    val pages: List<P>,
    val items: List<I>
) {
    constructor(
        pages: List<P>,
        idExtractor: ((I) -> Any)? = null
    ) : this(pages, getItems(pages, idExtractor))

    val hasNextPage get() = pages.lastOrNull()?.hasNextPage ?: false

    val hasPreviousPage get() = pages.firstOrNull()?.hasPreviousPage ?: false
}

private fun <I : Any, P : Page<I>> getItems(pages: List<P>, idExtractor: ((I) -> Any)? = null): List<I> {
    val items = pages.asSequence().flatMap { it.items }
    return if (idExtractor == null) {
        items.toList()
    } else {
        items.distinctBy { idExtractor(it) }.toList()
    }
}