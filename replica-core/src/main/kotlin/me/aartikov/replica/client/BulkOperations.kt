package me.aartikov.replica.client

import me.aartikov.replica.common.InvalidationMode
import me.aartikov.replica.common.ReplicaTag
import me.aartikov.replica.keyed.*

suspend fun ReplicaClient.cancelAll() {
    onEachReplica(includeChildrenOfKeyedReplicas = false) {
        cancel()
    }

    onEachKeyedReplica {
        this.cancelAll()
    }
}

suspend fun ReplicaClient.clearAll() {
    onEachReplica(includeChildrenOfKeyedReplicas = false) {
        clear()
    }

    onEachKeyedReplica {
        this.clearAll()
    }
}

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