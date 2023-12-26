package me.aartikov.replica.paged

import kotlinx.datetime.Instant
import me.aartikov.replica.common.OptimisticUpdate
import me.aartikov.replica.common.applyAll

data class PagedReplicaData<T : Any, P : Page<T>>(
    val value: PagedData<T, P>,
    val fresh: Boolean,
    val changingTime: Instant,
    val optimisticUpdates: List<OptimisticUpdate<List<P>>> = emptyList()
) {
    val valueWithOptimisticUpdates: PagedData<T, P> by lazy {
        PagedData(optimisticUpdates.applyAll(value.pages))
    }
}