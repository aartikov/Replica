package me.aartikov.replica.keyed_paged

import me.aartikov.replica.paged.Page

/**
 * Configures behaviour of a keyed replica.
 * @property maxCount limits count of child replicas
 * @property clearPolicy configures how keyed replica clears children when child count exceeds [maxCount].
 * See: [PagedClearPolicy].
 */
data class KeyedPagedReplicaSettings<K : Any, I : Any, P : Page<I>>(
    val maxCount: Int = Int.MAX_VALUE,
    val clearPolicy: PagedClearPolicy<K, I, P> = PagedClearPolicy()
)