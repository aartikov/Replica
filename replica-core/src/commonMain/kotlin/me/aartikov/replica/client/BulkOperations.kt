package me.aartikov.replica.client

import me.aartikov.replica.common.InvalidationMode
import me.aartikov.replica.common.ReplicaTag
import me.aartikov.replica.keyed.cancelAll
import me.aartikov.replica.keyed.cancelByTags
import me.aartikov.replica.keyed.clearByTags
import me.aartikov.replica.keyed.invalidateAll
import me.aartikov.replica.keyed.invalidateByTags
import me.aartikov.replica.keyed_paged.cancelAll
import me.aartikov.replica.keyed_paged.cancelByTags
import me.aartikov.replica.keyed_paged.clearByTags
import me.aartikov.replica.keyed_paged.invalidateAll
import me.aartikov.replica.keyed_paged.invalidateByTags

/**
 * Cancels network requests in all replicas.
 */
suspend fun ReplicaClient.cancelAll() {
    onEachReplica(includeChildrenOfKeyedReplicas = false) {
        this.cancel()
    }

    onEachKeyedReplica {
        this.cancelAll()
    }

    onEachPagedReplica(includeChildrenOfKeyedReplicas = false) {
        this.cancel()
    }

    onEachKeyedPagedReplica {
        this.cancelAll()
    }
}

/**
 * Cancels network requests and clears data in all replicas.
 */
suspend fun ReplicaClient.clearAll() {
    onEachReplica(includeChildrenOfKeyedReplicas = false) {
        this.clear()
    }

    onEachKeyedReplica {
        this.clearAll()
    }

    onEachPagedReplica(includeChildrenOfKeyedReplicas = false) {
        this.clear()
    }

    onEachKeyedPagedReplica {
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
        this.invalidate(mode)
    }

    onEachKeyedReplica {
        this.invalidateAll(mode)
    }

    onEachPagedReplica(includeChildrenOfKeyedReplicas = false) {
        this.invalidate(mode)
    }

    onEachKeyedPagedReplica {
        this.invalidateAll(mode)
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
            this.cancel()
        }
    }

    onEachKeyedReplica {
        this.cancelByTags(predicate)
    }

    onEachPagedReplica(includeChildrenOfKeyedReplicas = false) {
        if (predicate(tags)) {
            this.cancel()
        }
    }

    onEachKeyedPagedReplica {
        this.cancelByTags(predicate)
    }
}

/**
 * Cancels network requests and clears data in replicas with the matching tags.
 */
suspend fun ReplicaClient.clearByTags(predicate: (Set<ReplicaTag>) -> Boolean) {
    onEachReplica(includeChildrenOfKeyedReplicas = false) {
        if (predicate(tags)) {
            this.clear()
        }
    }

    onEachKeyedReplica {
        this.clearByTags(predicate)
    }

    onEachPagedReplica(includeChildrenOfKeyedReplicas = false) {
        if (predicate(tags)) {
            this.clear()
        }
    }

    onEachKeyedPagedReplica {
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
            this.invalidate(mode)
        }
    }

    onEachKeyedReplica {
        this.invalidateByTags(mode, predicate)
    }

    onEachPagedReplica(includeChildrenOfKeyedReplicas = false) {
        if (predicate(tags)) {
            this.invalidate(mode)
        }
    }

    onEachKeyedPagedReplica {
        this.invalidateByTags(mode, predicate)
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
suspend fun ReplicaClient.clearAndInvalidateByTag(
    mode: InvalidationMode = InvalidationMode.RefreshIfHasObservers,
    tag: ReplicaTag
) {
    clearByTag(tag)
    invalidateByTag(mode, tag)
}
