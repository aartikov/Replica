package me.aartikov.replica.keyed

import me.aartikov.replica.common.OptimisticUpdate
import me.aartikov.replica.common.performOptimisticUpdate

suspend inline fun <K : Any, T : Any, R> withOptimisticUpdate(
    update: OptimisticUpdate<T>,
    replica: KeyedPhysicalReplica<K, T>,
    key: K,
    noinline onSuccess: (suspend () -> Unit)? = null,
    noinline onError: (suspend (Exception) -> Unit)? = null,
    noinline onCanceled: (suspend () -> Unit)? = null,
    noinline onCompleted: (suspend () -> Unit)? = null,
    block: () -> R
): R {
    return performOptimisticUpdate(
        begin = { replica.beginOptimisticUpdate(key, update) },
        commit = { replica.commitOptimisticUpdate(key, update) },
        rollback = { replica.rollbackOptimisticUpdate(key, update) },
        onSuccess = onSuccess,
        onError = onError,
        onCanceled = onCanceled,
        onCompleted = onCompleted,
        block = block
    )
}