package me.aartikov.replica.common.internal

internal actual class AtomicLong actual constructor(initialValue: Long) {

    actual var value: Long = initialValue

    actual fun addAndGet(delta: Long): Long {
        value += delta
        return value
    }
}