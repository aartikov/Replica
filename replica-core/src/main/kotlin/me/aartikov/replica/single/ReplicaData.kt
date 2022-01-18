package me.aartikov.replica.single

import kotlinx.datetime.Instant

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