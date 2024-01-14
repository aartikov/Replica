package me.aartikov.replica.advanced_sample.features.dudes.domain

data class DudesContent(
    val items: List<Dude>,
    val hasNextPage: Boolean
)