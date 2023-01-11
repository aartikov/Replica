package me.aartikov.replica.common

import me.aartikov.replica.common.internal.AtomicLong
import kotlin.jvm.JvmInline

/**
 * Unique identifier for a replica.
 */
@JvmInline
value class ReplicaId(val value: Long) {
    companion object {
        private val idGenerator = AtomicLong(0)
        fun random() = ReplicaId(idGenerator.addAndGet(1))
    }
}