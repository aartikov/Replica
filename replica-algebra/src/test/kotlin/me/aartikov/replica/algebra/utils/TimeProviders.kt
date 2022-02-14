package me.aartikov.replica.algebra.utils

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.currentTime
import kotlinx.datetime.Instant
import me.aartikov.replica.time.TimeProvider

@OptIn(ExperimentalCoroutinesApi::class)
class VirtualTimeProvider(val scope: TestScope) : TimeProvider {
    override val currentTime: Instant
        get() = Instant.fromEpochMilliseconds(scope.currentTime)
}

class FakeTimeProvider : TimeProvider {
    override var currentTime: Instant = Instant.fromEpochMilliseconds(1000)
}