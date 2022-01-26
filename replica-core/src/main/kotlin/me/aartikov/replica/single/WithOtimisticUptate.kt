package me.aartikov.replica.keyed

import me.aartikov.replica.common.OptimisticUpdate
import me.aartikov.replica.common.performOptimisticUpdate
import me.aartikov.replica.single.PhysicalReplica

suspend inline fun <T : Any, R> withOptimisticUpdate(
    update: OptimisticUpdate<T>,
    replica: PhysicalReplica<T>,
    noinline onSuccess: (suspend () -> Unit)? = null,
    noinline onError: (suspend (Exception) -> Unit)? = null,
    noinline onCanceled: (suspend () -> Unit)? = null,
    noinline onCompleted: (suspend () -> Unit)? = null,
    block: () -> R
): R {
    return performOptimisticUpdate(
        begin = { replica.beginOptimisticUpdate(update) },
        commit = { replica.commitOptimisticUpdate(update) },
        rollback = { replica.rollbackOptimisticUpdate(update) },
        onSuccess = onSuccess,
        onError = onError,
        onCanceled = onCanceled,
        onCompleted = onCompleted,
        block = block
    )
}