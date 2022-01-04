package me.aartikov.replica.single

import me.aartikov.replica.keyed.KeyedPhysicalReplica

fun interface OptimisticUpdate<T : Any> {
    fun apply(data: T): T
}

fun <T : Any> List<OptimisticUpdate<T>>.applyAll(data: T): T {
    return fold(data, { d, u -> u.apply(d) })
}

inline fun <R> performOptimisticUpdate(
    begin: () -> Unit,
    commit: () -> Unit,
    rollback: () -> Unit,
    block: () -> R
): R {
    try {
        begin()
        val result = block()
        commit()
        return result
    } catch (e: Exception) {
        rollback()
        throw e
    }
}

suspend inline fun <T : Any, R> withOptimisticUpdate(
    update: OptimisticUpdate<T>,
    replica: PhysicalReplica<T>,
    block: () -> R
): R {
    return performOptimisticUpdate(
        begin = { replica.beginOptimisticUpdate(update) },
        commit = { replica.commitOptimisticUpdate(update) },
        rollback = { replica.rollbackOptimisticUpdate(update) },
        block = block
    )
}

suspend inline fun <K : Any, T : Any, R> withOptimisticUpdate(
    update: OptimisticUpdate<T>,
    replica: KeyedPhysicalReplica<K, T>,
    key: K,
    block: () -> R
): R {
    return performOptimisticUpdate(
        begin = { replica.beginOptimisticUpdate(key, update) },
        commit = { replica.commitOptimisticUpdate(key, update) },
        rollback = { replica.rollbackOptimisticUpdate(key, update) },
        block = block
    )
}