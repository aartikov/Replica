package me.aartikov.replica.keyed

import me.aartikov.replica.common.OptimisticUpdate
import me.aartikov.replica.common.performOptimisticUpdate

/**
 * Executes an optimistic update on a [KeyedPhysicalReplica] for a given [key].
 * [update] is applied immediately on observed replica state. Then [block] is executed.
 * If [block] succeed an update is committed, otherwise an update is rolled back.
 *
 * [onSuccess], [onError], [onCanceled] are optional callbacks for additional actions.
 */
suspend inline fun <K : Any, T : Any, R> KeyedPhysicalReplica<K, T>.withOptimisticUpdate(
    key: K,
    update: OptimisticUpdate<T>,
    noinline onSuccess: (suspend () -> Unit)? = null,
    noinline onError: (suspend (Exception) -> Unit)? = null,
    noinline onCanceled: (suspend () -> Unit)? = null,
    block: suspend () -> R
): R {
    return performOptimisticUpdate(
        begin = { beginOptimisticUpdate(key, update, operationId = update) },
        commit = { commitOptimisticUpdate(key, update) },
        rollback = { rollbackOptimisticUpdate(key, update) },
        onSuccess = onSuccess,
        onError = onError,
        onCanceled = onCanceled,
        block = block
    )
}