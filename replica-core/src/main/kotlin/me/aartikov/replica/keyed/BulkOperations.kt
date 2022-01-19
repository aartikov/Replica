package me.aartikov.replica.keyed

import me.aartikov.replica.common.InvalidationMode

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