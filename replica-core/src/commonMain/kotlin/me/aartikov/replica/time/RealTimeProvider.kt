package me.aartikov.replica.time

import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Provides current time from a system clock.
 */
class RealTimeProvider : TimeProvider {

    override val currentTime: Instant
        get() = Clock.System.now()
}