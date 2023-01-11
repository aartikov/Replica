package me.aartikov.replica.common.internal

import platform.Foundation.NSRecursiveLock

internal actual class Lock actual constructor() {
    private val impl = NSRecursiveLock()

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