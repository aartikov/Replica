package me.aartikov.replica.advanced_sample.features.dudes.domain

import me.aartikov.replica.paged.Page


data class DudesPage(
    override val items: List<Dude>,
    val nextPageCursor: String?
) : Page<Dude> {

    override val hasNextPage get() = nextPageCursor != null

    override val hasPreviousPage get() = false
}