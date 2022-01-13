package me.aartikov.replica.keyed

import java.util.*

@JvmInline
value class KeyedReplicaId(val value: String) {
    companion object {
        fun random() = KeyedReplicaId(UUID.randomUUID().toString())
    }
}