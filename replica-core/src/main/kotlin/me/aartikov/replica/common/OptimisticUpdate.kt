package me.aartikov.replica.common

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import me.aartikov.replica.keyed.KeyedPhysicalReplica
import me.aartikov.replica.single.PhysicalReplica

fun interface OptimisticUpdate<T : Any> {
    fun apply(data: T): T
}

fun <T : Any> List<OptimisticUpdate<T>>.applyAll(data: T): T {
    return fold(data, { d, u -> u.apply(d) })
}

suspend inline fun <R> performOptimisticUpdate(
    begin: () -> Unit,
    commit: () -> Unit,
    crossinline rollback: suspend () -> Unit,
    noinline onSuccess: (suspend () -> Unit)? = null,
    noinline onError: (suspend (Exception) -> Unit)? = null,
    noinline onCanceled: (suspend () -> Unit)? = null,
    noinline onCompleted: (suspend () -> Unit)? = null,
    block: () -> R
): R {
    try {
        begin()
        val result = block()
        commit()
        onSuccess?.invoke()
        onCompleted?.invoke()
        return result
    } catch (e: CancellationException) {
        withContext(NonCancellable) {
            rollback()
            onCanceled?.invoke()
            onCompleted?.invoke()
        }
        throw e
    } catch (e: Exception) {
        rollback()
        onError?.invoke(e)
        onCompleted?.invoke()
        throw e
    }
}

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