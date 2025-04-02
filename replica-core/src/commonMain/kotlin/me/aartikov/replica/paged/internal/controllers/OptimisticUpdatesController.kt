package me.aartikov.replica.paged.internal.controllers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import me.aartikov.replica.common.OptimisticUpdate
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedData
import me.aartikov.replica.paged.PagedReplicaState
import me.aartikov.replica.time.TimeProvider

internal class OptimisticUpdatesController<I : Any, P : Page<I>>(
    private val timeProvider: TimeProvider,
    private val dispatcher: CoroutineDispatcher,
    private val idExtractor: ((I) -> Any)?,
    private val replicaStateFlow: MutableStateFlow<PagedReplicaState<I, P>>
) {

    suspend fun beginOptimisticUpdate(update: OptimisticUpdate<List<P>>, operationId: Any) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            if (state.data != null) {
                replicaStateFlow.value = state.copy(
                    data = state.data.copy(
                        optimisticUpdates = state.data.optimisticUpdates - operationId + (operationId to update)
                    )
                )
            }
        }
    }

    suspend fun commitOptimisticUpdate(operationId: Any) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            val update = state.data?.optimisticUpdates?.get(operationId)
            if (update != null) {
                val newData = update.apply(state.data.value.pages)
                replicaStateFlow.value = state.copy(
                    data = state.data.copy(
                        value = PagedData(newData, idExtractor),
                        optimisticUpdates = state.data.optimisticUpdates - operationId,
                        changingTime = timeProvider.currentTime
                    )
                )
            }
        }
    }

    suspend fun rollbackOptimisticUpdate(operationId: Any) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            if (state.data != null) {
                replicaStateFlow.value = state.copy(
                    data = state.data.copy(
                        optimisticUpdates = state.data.optimisticUpdates - operationId
                    )
                )
            }
        }
    }
}