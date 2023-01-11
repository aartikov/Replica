package me.aartikov.replica.common.internal

internal actual class AtomicLong actual constructor(initialValue: Long) {

    private val impl = kotlin.native.concurrent.AtomicLong(initialValue)

    actual var value: Long
        get() = impl.value
        set(value) {
            impl.value = value
        }

    actual fun addAndGet(delta: Long): Long {
        return impl.addAndGet(delta)
    }
}