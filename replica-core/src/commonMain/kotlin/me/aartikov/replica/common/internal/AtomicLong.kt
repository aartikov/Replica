package me.aartikov.replica.common.internal

internal expect class AtomicLong(initialValue: Long = 0L) {

    var value: Long

    fun addAndGet(delta: Long): Long

}