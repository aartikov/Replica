package me.aartikov.replica.client

import me.aartikov.replica.common.InvalidationMode
import me.aartikov.replica.common.ReplicaTag
import me.aartikov.replica.keyed.*

/**
 * Cancels network requests in all replicas.
 */
suspend fun ReplicaClient.cancelAll() {
    onEachReplica(includeChildrenOfKeyedReplicas = false) {
        cancel()
    }

    onEachKeyedReplica {
        this.cancelAll()
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
        this.clearAll()
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
        this.invalidateAll(mode)
    }
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
        this.cancelByTags(predicate)
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
        this.clearByTags(predicate)
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
        this.invalidateByTags(mode, predicate)
    }
}