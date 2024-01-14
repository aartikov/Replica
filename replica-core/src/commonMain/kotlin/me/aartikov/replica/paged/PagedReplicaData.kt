package me.aartikov.replica.paged

import kotlinx.datetime.Instant
import me.aartikov.replica.common.OptimisticUpdate
import me.aartikov.replica.common.applyAll

data class PagedReplicaData<I : Any, P : Page<I>>(
    val value: PagedData<I, P>,
    val fresh: Boolean,
    val changingTime: Instant,
    val idExtractor: ((I) -> Any)?,
    val optimisticUpdates: List<OptimisticUpdate<List<P>>> = emptyList()
) {
    val valueWithOptimisticUpdates: PagedData<I, P> by lazy {
        PagedData(optimisticUpdates.applyAll(value.pages), idExtractor)
    }
}