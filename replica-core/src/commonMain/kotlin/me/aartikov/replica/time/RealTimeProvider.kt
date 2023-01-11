package me.aartikov.replica.time

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Provides current time from a system clock.
 */
class RealTimeProvider : TimeProvider {

    override val currentTime: Instant
        get() = Clock.System.now()
}