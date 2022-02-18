package me.aartikov.replica.algebra.utils

import kotlinx.datetime.Instant
import me.aartikov.replica.time.TimeProvider

class FakeTimeProvider : TimeProvider {
    override var currentTime: Instant = Instant.fromEpochMilliseconds(1000)
}