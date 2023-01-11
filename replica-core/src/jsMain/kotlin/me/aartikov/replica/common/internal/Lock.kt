package me.aartikov.replica.common.internal

// JS is single threaded
internal actual class Lock actual constructor() {

    actual fun lock() {}

    actual fun unlock() {}

    actual fun tryLock(): Boolean = true
}