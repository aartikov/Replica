package me.aartikov.replica.common.internal

internal actual class AtomicLong actual constructor(initialValue: Long) {

    private val impl = java.util.concurrent.atomic.AtomicLong(initialValue)

    actual var value: Long
        get() = impl.get()
        set(value) {
            impl.set(value)
        }

    actual fun addAndGet(delta: Long): Long {
        return impl.addAndGet(delta)
    }
}