package me.aartikov.replica.single

import java.util.*

@JvmInline
value class ReplicaId(val value: String) {
    companion object {
        fun random() = ReplicaId(UUID.randomUUID().toString())
    }
}