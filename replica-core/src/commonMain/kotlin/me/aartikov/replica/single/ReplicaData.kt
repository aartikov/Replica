package me.aartikov.replica.single

import kotlinx.datetime.Instant
import me.aartikov.replica.common.OptimisticUpdate
import me.aartikov.replica.common.applyAll

/**
 * Data stored in a replica.
 */
data class ReplicaData<T : Any>(
    val value: T,
    val fresh: Boolean,
    val changingTime: Instant,
    val optimisticUpdates: List<OptimisticUpdate<T>> = emptyList()
) {
    val valueWithOptimisticUpdates by lazy {
        optimisticUpdates.applyAll(value)
    }
}