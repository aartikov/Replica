package me.aartikov.replica.keyed

data class KeyedReplicaSettings<K : Any, T : Any>(
    val maxCount: Int = Int.MAX_VALUE,
    val clearPolicy: ClearPolicy<K, T> = ClearPolicy()
)