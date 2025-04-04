package me.aartikov.replica.keyed_paged

import me.aartikov.replica.common.OptimisticUpdate
import me.aartikov.replica.common.performOptimisticUpdate
import me.aartikov.replica.paged.Page

/**
 * Executes an optimistic update on a [KeyedPagedPhysicalReplica] for a given [key].
 * [update] is applied immediately on observed replica state. Then [block] is executed.
 * If [block] succeed an update is committed, otherwise an update is rolled back.
 *
 * [onSuccess], [onError], [onCanceled], [onFinished] are optional callbacks for additional actions.
 */
suspend inline fun <K : Any, I : Any, P : Page<I>, R> KeyedPagedPhysicalReplica<K, I, P>.withOptimisticUpdate(
    key: K,
    update: OptimisticUpdate<List<P>>,
    noinline onSuccess: (suspend () -> Unit)? = null,
    noinline onError: (suspend (Exception) -> Unit)? = null,
    noinline onCanceled: (suspend () -> Unit)? = null,
    noinline onFinished: (suspend () -> Unit)? = null,
    block: () -> R
): R {
    return performOptimisticUpdate(
        begin = { beginOptimisticUpdate(key, update, operationId = update) },
        commit = { commitOptimisticUpdate(key, update) },
        rollback = { rollbackOptimisticUpdate(key, update) },
        onSuccess = onSuccess,
        onError = onError,
        onCanceled = onCanceled,
        onFinished = onFinished,
        block = block
    )
}