package me.aartikov.replica.keyed_paged

import me.aartikov.replica.paged.Page

/**
 * Configures behaviour of a keyed replica.
 * @property maxCount limits count of child replicas
 * @property clearPolicy configures how keyed replica clears children when child count exceeds [maxCount].
 * See: [PagedClearPolicy].
 */
data class KeyedPagedReplicaSettings<K : Any, T : Any, P : Page<T>>(
    val maxCount: Int = Int.MAX_VALUE,
    val clearPolicy: PagedClearPolicy<K, T, P> = PagedClearPolicy()
)