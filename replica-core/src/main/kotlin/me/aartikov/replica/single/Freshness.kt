package me.aartikov.replica.single

sealed class Freshness {
    object Fresh : Freshness() // TODO: add refresh time
    object Stale : Freshness()
}
