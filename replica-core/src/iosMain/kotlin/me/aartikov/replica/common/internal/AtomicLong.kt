package me.aartikov.replica.common.internal

import kotlin.concurrent.AtomicLong

internal actual class AtomicLong actual constructor(initialValue: Long) {

    private val impl = AtomicLong(initialValue)

    actual var value: Long
        get() = impl.value
        set(value) {
            impl.value = value
        }

    actual fun addAndGet(delta: Long): Long {
        return impl.addAndGet(delta)
    }
}