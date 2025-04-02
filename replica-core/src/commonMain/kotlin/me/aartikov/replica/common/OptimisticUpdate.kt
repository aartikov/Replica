package me.aartikov.replica.common

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

/**
 * Action executed for optimistic update
 */
fun interface OptimisticUpdate<T : Any> {
    fun apply(data: T): T
}

fun <T : Any> Collection<OptimisticUpdate<T>>.applyAll(data: T): T {
    return fold(data, { d, u -> u.apply(d) })
}

/**
 * Executes [begin] and then [block]. If an operation succeed than [commit] is executed, otherwise [rollback] is executed.
 * [onSuccess], [onError], [onCanceled], [onFinished] are optional callbacks for additional actions.
 */
suspend inline fun <R> performOptimisticUpdate(
    begin: () -> Unit,
    commit: () -> Unit,
    crossinline rollback: suspend () -> Unit,
    noinline onSuccess: (suspend () -> Unit)? = null,
    noinline onError: (suspend (Exception) -> Unit)? = null,
    noinline onCanceled: (suspend () -> Unit)? = null,
    noinline onFinished: (suspend () -> Unit)? = null,
    block: () -> R
): R {
    try {
        begin()
        val result = block()
        commit()
        onSuccess?.invoke()
        onFinished?.invoke()
        return result
    } catch (e: CancellationException) {
        withContext(NonCancellable) {
            rollback()
            onCanceled?.invoke()
            onFinished?.invoke()
        }
        throw e
    } catch (e: Exception) {
        rollback()
        onError?.invoke(e)
        onFinished?.invoke()
        throw e
    }
}