package me.aartikov.replica.keyed

import me.aartikov.replica.common.InvalidationMode
import me.aartikov.replica.common.ReplicaTag

/**
 * Cancels network requests in all child replicas.
 */
suspend fun <K : Any, T : Any> KeyedPhysicalReplica<T, K>.cancelAll() {
    onEachReplica {
        cancel()
    }
}

/**
 * Makes all child replicas stale.
 *
 * @param mode specifies how replicas behave after invalidation. See: [InvalidationMode].
 */
suspend fun <K : Any, T : Any> KeyedPhysicalReplica<T, K>.invalidateAll(
    mode: InvalidationMode = InvalidationMode.RefreshIfHasObservers
) {
    onEachReplica {
        invalidate(mode)
    }
}

/**
 * Cancels network requests in child replicas with the matching tags.
 */
suspend fun <K : Any, T : Any> KeyedPhysicalReplica<T, K>.cancelByTags(
    predicate: (Set<ReplicaTag>) -> Boolean
) {
    if (predicate(tags)) {
        cancelAll()
        return
    }

    onEachReplica {
        if (predicate(tags)) {
            cancel()
        }
    }
}

/**
 * Cancels network requests and clears data in child replicas with the matching tags.
 */
suspend fun <K : Any, T : Any> KeyedPhysicalReplica<T, K>.clearByTags(
    predicate: (Set<ReplicaTag>) -> Boolean
) {
    if (predicate(tags)) {
        clearAll()
        return
    }

    onEachReplica {
        if (predicate(tags)) {
            clear()
        }
    }
}

/**
 * Makes child replicas with the matching tags stale.
 *
 * @param mode specifies how replicas behave after invalidation. See: [InvalidationMode].
 */
suspend fun <K : Any, T : Any> KeyedPhysicalReplica<T, K>.invalidateByTags(
    mode: InvalidationMode = InvalidationMode.RefreshIfHasObservers,
    predicate: (Set<ReplicaTag>) -> Boolean
) {
    if (predicate(tags)) {
        invalidateAll(mode)
        return
    }

    onEachReplica {
        if (predicate(tags)) {
            invalidate(mode)
        }
    }
}