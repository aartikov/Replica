package me.aartikov.replica.time

import kotlin.time.Instant

/**
 * Provides current time (as [Instant]).
 */
interface TimeProvider {
    val currentTime: Instant
}