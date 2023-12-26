package me.aartikov.replica.paged

data class PagedData<out T : Any, out P : Page<T>>(
    val pages: List<P>,
    val items: List<T>
) {
    constructor(pages: List<P>) : this(pages, emptyList()) // TODO: items
}