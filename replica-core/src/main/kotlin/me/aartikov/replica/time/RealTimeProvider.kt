package me.aartikov.replica.time

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class RealTimeProvider : TimeProvider {

    override val currentTime: Instant
        get() = Clock.System.now()
}