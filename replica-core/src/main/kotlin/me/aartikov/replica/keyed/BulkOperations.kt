package me.aartikov.replica.keyed

import me.aartikov.replica.common.InvalidationMode
import me.aartikov.replica.common.ReplicaTag

suspend fun <K : Any, T : Any> KeyedPhysicalReplica<T, K>.cancelAll() {
    onEachReplica {
        cancel()
    }
}

suspend fun <K : Any, T : Any> KeyedPhysicalReplica<T, K>.invalidateAll(
    mode: InvalidationMode = InvalidationMode.RefreshIfHasObservers
) {
    onEachReplica {
        invalidate(mode)
    }
}

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