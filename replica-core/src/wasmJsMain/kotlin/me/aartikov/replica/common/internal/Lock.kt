package me.aartikov.replica.common.internal

// Kotlin/Wasm in the browser is single threaded
internal actual class Lock actual constructor() {

    actual fun lock() {}

    actual fun unlock() {}

    actual fun tryLock(): Boolean = true
}
