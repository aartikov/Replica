package me.aartikov.replica.single

data class ReplicaData<T : Any>(
    val value: T,
    val freshness: Freshness,
    val optimisticUpdates: List<OptimisticUpdate<T>> = emptyList()
) {

    val fresh: Boolean get() = freshness is Freshness.Fresh

    val valueWithOptimisticUpdates by lazy {
        optimisticUpdates.applyAll(value)
    }
}