package me.aartikov.replica.client

import me.aartikov.replica.common.InvalidationMode
import me.aartikov.replica.common.ReplicaTag
import me.aartikov.replica.keyed.cancelAll
import me.aartikov.replica.keyed.cancelByTags
import me.aartikov.replica.keyed.clearByTags
import me.aartikov.replica.keyed.invalidateAll
import me.aartikov.replica.keyed.invalidateByTags

/**
 * Cancels network requests in all replicas.
 */
suspend fun ReplicaClient.cancelAll() {
    onEachReplica(includeChildrenOfKeyedReplicas = false) {
        cancel()
    }

    onEachKeyedReplica {
        cancelAll()
    }
}

/**
 * Cancels network requests and clears data in all replicas.
 */
suspend fun ReplicaClient.clearAll() {
    onEachReplica(includeChildrenOfKeyedReplicas = false) {
        clear()
    }

    onEachKeyedReplica {
        clearAll()
    }
}

/**
 * Makes all replicas stale.
 *
 * @param mode specifies how replicas behave after invalidation. See: [InvalidationMode].
 */
suspend fun ReplicaClient.invalidateAll(
    mode: InvalidationMode = InvalidationMode.RefreshIfHasObservers
) {
    onEachReplica(includeChildrenOfKeyedReplicas = false) {
        invalidate(mode)
    }

    onEachKeyedReplica {
        invalidateAll(mode)
    }
}

/**
 * Clears and invalidates all replicas
 *
 * @param mode specifies how replicas behave after invalidation. See: [InvalidationMode].
 */
suspend fun ReplicaClient.clearAndInvalidateAll(
    mode: InvalidationMode = InvalidationMode.RefreshIfHasObservers
) {
    clearAll()
    invalidateAll(mode)
}

/**
 * Cancels network requests in replicas with the matching tags.
 */
suspend fun ReplicaClient.cancelByTags(predicate: (Set<ReplicaTag>) -> Boolean) {
    onEachReplica(includeChildrenOfKeyedReplicas = false) {
        if (predicate(tags)) {
            cancel()
        }
    }

    onEachKeyedReplica {
        cancelByTags(predicate)
    }
}

/**
 * Cancels network requests and clears data in replicas with the matching tags.
 */
suspend fun ReplicaClient.clearByTags(predicate: (Set<ReplicaTag>) -> Boolean) {
    onEachReplica(includeChildrenOfKeyedReplicas = false) {
        if (predicate(tags)) {
            clear()
        }
    }

    onEachKeyedReplica {
        clearByTags(predicate)
    }
}

/**
 * Makes replicas with the matching tags stale.
 *
 * @param mode specifies how replicas behave after invalidation. See: [InvalidationMode].
 */
suspend fun ReplicaClient.invalidateByTags(
    mode: InvalidationMode = InvalidationMode.RefreshIfHasObservers,
    predicate: (Set<ReplicaTag>) -> Boolean
) {
    onEachReplica(includeChildrenOfKeyedReplicas = false) {
        if (predicate(tags)) {
            invalidate(mode)
        }
    }

    onEachKeyedReplica {
        invalidateByTags(mode, predicate)
    }
}

/**
 * Clears and invalidates replicas with the matching tags.
 *
 * @param mode specifies how replicas behave after invalidation. See: [InvalidationMode].
 */
suspend fun ReplicaClient.clearAndInvalidateByTags(
    mode: InvalidationMode = InvalidationMode.RefreshIfHasObservers,
    predicate: (Set<ReplicaTag>) -> Boolean
) {
    clearByTags(predicate)
    invalidateByTags(mode, predicate)
}


/**
 * Cancels network requests in replicas with a given [tag].
 */
suspend fun ReplicaClient.cancelByTag(tag: ReplicaTag) {
    cancelByTags { it.contains(tag) }
}

/**
 * Cancels network requests and clears data in replicas with a given [tag].
 */
suspend fun ReplicaClient.clearByTag(tag: ReplicaTag) {
    clearByTags { it.contains(tag) }
}

/**
 * Makes replicas with a given [tag] stale.
 *
 * @param mode specifies how replicas behave after invalidation. See: [InvalidationMode].
 */
suspend fun ReplicaClient.invalidateByTag(
    mode: InvalidationMode = InvalidationMode.RefreshIfHasObservers,
    tag: ReplicaTag
) {
    invalidateByTags(mode) { it.contains(tag) }
}

/**
 * Clears and invalidates replicas with a given [tag] stale.
 *
 * @param mode specifies how replicas behave after invalidation. See: [InvalidationMode].
 */
suspend fun ReplicaClient.clearAndInvalidateByTags(
    mode: InvalidationMode = InvalidationMode.RefreshIfHasObservers,
    tag: ReplicaTag
) {
    clearByTag(tag)
    invalidateByTag(mode, tag)
}
