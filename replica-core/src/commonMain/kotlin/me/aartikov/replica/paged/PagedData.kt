package me.aartikov.replica.paged

data class PagedData<out T : Any, out P : Page<T>>(
    val pages: List<P>,
    val items: List<T>
) {
    constructor(
        pages: List<P>,
        idExtractor: ((T) -> Any)? = null
    ) : this(pages, getItems(pages, idExtractor))

    val hasNextPage get() = pages.lastOrNull()?.hasNextPage ?: false

    val hasPreviousPage get() = pages.firstOrNull()?.hasPreviousPage ?: false
}

private fun <T : Any, P : Page<T>> getItems(pages: List<P>, idExtractor: ((T) -> Any)? = null): List<T> {
    val items = pages.asSequence().flatMap { it.items }
    return if (idExtractor == null) {
        items.toList()
    } else {
        items.distinctBy { idExtractor(it) }.toList()
    }
}