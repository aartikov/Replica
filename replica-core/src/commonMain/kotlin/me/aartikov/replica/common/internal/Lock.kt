package me.aartikov.replica.common.internal

internal expect class Lock() {
    fun lock()
    fun unlock()
    fun tryLock(): Boolean
}

internal inline fun <T> Lock.withLock(block: () -> T): T {
    lock()
    try {
        return block()
    } finally {
        unlock()
    }
}