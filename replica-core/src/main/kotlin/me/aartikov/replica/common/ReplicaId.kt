package me.aartikov.replica.common

import java.util.*

/**
 * Unique identifier for a replica.
 */
@JvmInline
value class ReplicaId(val value: String) {
    companion object {
        fun random() = ReplicaId(UUID.randomUUID().toString())
    }
}