package me.aartikov.replica.keyed

import me.aartikov.replica.common.OptimisticUpdate
import me.aartikov.replica.common.performOptimisticUpdate

/**
 * Executes an optimistic update on a [KeyedPhysicalReplica] for a given [key].
 * [operationId] is the identifier of the operation being executed.
 * If you don't pass [operationId] explicitly then [update] is used as [operationId].
 * [update] is applied immediately on observed replica state. Then [block] is executed.
 * If [block] succeed an update is committed, otherwise an update is rolled back.
 *
 * [onSuccess], [onError], [onCanceled], [onFinished] are optional callbacks for additional actions.
 *
 * Note: An update with the same [operationId] will replace the previous update.
 */
suspend inline fun <K : Any, T : Any, R> KeyedPhysicalReplica<K, T>.withOptimisticUpdate(
    update: OptimisticUpdate<T>,
    key: K,
    operationId: Any = update,
    noinline onSuccess: (suspend () -> Unit)? = null,
    noinline onError: (suspend (Exception) -> Unit)? = null,
    noinline onCanceled: (suspend () -> Unit)? = null,
    noinline onFinished: (suspend () -> Unit)? = null,
    block: () -> R
): R {
    return performOptimisticUpdate(
        begin = { beginOptimisticUpdate(key, update, operationId) },
        commit = { commitOptimisticUpdate(key, operationId) },
        rollback = { rollbackOptimisticUpdate(key, operationId) },
        onSuccess = onSuccess,
        onError = onError,
        onCanceled = onCanceled,
        onFinished = onFinished,
        block = block
    )
}