package me.aartikov.replica.client

import me.aartikov.replica.common.InvalidationMode
import me.aartikov.replica.keyed.cancelAll
import me.aartikov.replica.keyed.invalidateAll

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