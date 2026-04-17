package me.aartikov.replica.algebra.utils

import kotlin.time.Instant
import me.aartikov.replica.time.TimeProvider

class FakeTimeProvider : TimeProvider {
    override var currentTime: Instant = Instant.fromEpochMilliseconds(1000)
}