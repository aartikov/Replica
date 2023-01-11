package me.aartikov.replica.common.internal

import java.util.concurrent.locks.ReentrantLock

internal actual class Lock actual constructor() {
    private val impl = ReentrantLock()

    actual fun lock() {
        impl.lock()
    }

    actual fun unlock() {
        impl.unlock()
    }

    actual fun tryLock(): Boolean {
        return impl.tryLock()
    }
}