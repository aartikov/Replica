package me.aartikov.replica.keyed_paged

import me.aartikov.replica.common.InvalidationMode
import me.aartikov.replica.common.ReplicaTag
import me.aartikov.replica.paged.Page

/**
 * Cancels network requests in all child replicas.
 */
suspend fun <K : Any, I : Any, P : Page<I>> KeyedPagedPhysicalReplica<K, I, P>.cancelAll() {
    onEachPagedReplica {
        cancel()
    }
}

/**
 * Makes all child replicas stale.
 *
 * @param mode specifies how replicas behave after invalidation. See: [InvalidationMode].
 */
suspend fun <K : Any, I : Any, P : Page<I>> KeyedPagedPhysicalReplica<K, I, P>.invalidateAll(
    mode: InvalidationMode = InvalidationMode.RefreshIfHasObservers
) {
    onEachPagedReplica {
        invalidate(mode)
    }
}

/**
 * Cancels network requests in child replicas with the matching tags.
 */
suspend fun <K : Any, I : Any, P : Page<I>> KeyedPagedPhysicalReplica<K, I, P>.cancelByTags(
    predicate: (Set<ReplicaTag>) -> Boolean
) {
    if (predicate(tags)) {
        cancelAll()
        return
    }

    onEachPagedReplica {
        if (predicate(tags)) {
            cancel()
        }
    }
}

/**
 * Cancels network requests and clears data in child replicas with the matching tags.
 */
suspend fun <K : Any, I : Any, P : Page<I>> KeyedPagedPhysicalReplica<K, I, P>.clearByTags(
    invalidationMode: InvalidationMode = InvalidationMode.DontRefresh,
    predicate: (Set<ReplicaTag>) -> Boolean
) {
    if (predicate(tags)) {
        clearAll(invalidationMode)
        return
    }

    onEachPagedReplica {
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
suspend fun <K : Any, I : Any, P : Page<I>> KeyedPagedPhysicalReplica<K, I, P>.invalidateByTags(
    mode: InvalidationMode = InvalidationMode.RefreshIfHasObservers,
    predicate: (Set<ReplicaTag>) -> Boolean
) {
    if (predicate(tags)) {
        invalidateAll(mode)
        return
    }

    onEachPagedReplica {
        if (predicate(tags)) {
            invalidate(mode)
        }
    }
}