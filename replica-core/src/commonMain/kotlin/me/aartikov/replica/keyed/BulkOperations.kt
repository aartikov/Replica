package me.aartikov.replica.keyed

import me.aartikov.replica.common.InvalidationMode
import me.aartikov.replica.common.ReplicaTag

/**
 * Cancels network requests in all child replicas.
 */
suspend fun <K : Any, T : Any> KeyedPhysicalReplica<K, T>.cancelAll() {
    onEachReplica {
        cancel()
    }
}

/**
 * Makes all child replicas stale.
 *
 * @param mode specifies how replicas behave after invalidation. See: [InvalidationMode].
 */
suspend fun <K : Any, T : Any> KeyedPhysicalReplica<K, T>.invalidateAll(
    mode: InvalidationMode = InvalidationMode.RefreshIfHasObservers
) {
    onEachReplica {
        invalidate(mode)
    }
}

/**
 * Cancels network requests in child replicas with the matching tags.
 */
suspend fun <K : Any, T : Any> KeyedPhysicalReplica<K, T>.cancelByTags(
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
 *
 * @param invalidationMode specifies how replicas refresh data. See: [InvalidationMode].
 */
suspend fun <K : Any, T : Any> KeyedPhysicalReplica<K, T>.clearByTags(
    invalidationMode: InvalidationMode = InvalidationMode.DontRefresh,
    predicate: (Set<ReplicaTag>) -> Boolean
) {
    if (predicate(tags)) {
        clearAll(invalidationMode)
        return
    }

    onEachReplica {
        if (predicate(tags)) {
            clear(invalidationMode)
        }
    }
}

/**
 * Makes child replicas with the matching tags stale.
 *
 * @param mode specifies how replicas behave after invalidation. See: [InvalidationMode].
 */
suspend fun <K : Any, T : Any> KeyedPhysicalReplica<K, T>.invalidateByTags(
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