package me.aartikov.replica.time

import kotlinx.datetime.Instant

/**
 * Provides current time (as [Instant]).
 */
interface TimeProvider {
    val currentTime: Instant
}