package me.aartikov.replica.keyed

/**
 * Configures behaviour of a keyed replica.
 * @property maxCount limits max count of child replicas
 * @property clearPolicy configures how keyed replica clears children when child count exceeds [maxCount]. See: [ClearOrder].
 */
data class KeyedReplicaSettings<K : Any, T : Any>(
    val maxCount: Int = Int.MAX_VALUE,
    val clearPolicy: ClearPolicy<K, T> = ClearPolicy()
)