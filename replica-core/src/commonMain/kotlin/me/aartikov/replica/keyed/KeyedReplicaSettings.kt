package me.aartikov.replica.keyed

/**
 * Configures behaviour of a keyed replica.
 * @property maxCount limits count of child replicas
 * @property clearPolicy configures how keyed replica clears children when child count exceeds [maxCount]. See: [ClearPolicy].
 */
data class KeyedReplicaSettings<K : Any, T : Any>(
    val maxCount: Int,
    val clearPolicy: ClearPolicy<K, T> = ClearPolicy(ClearOrder.ByObservingTime)
)