package me.aartikov.replica.single

data class ReplicaData<T : Any>(
    val value: T,
    val fresh: Boolean,
    val optimisticUpdates: List<OptimisticUpdate<T>> = emptyList()
) {

    val valueWithOptimisticUpdates by lazy {
        optimisticUpdates.applyAll(value)
    }
}