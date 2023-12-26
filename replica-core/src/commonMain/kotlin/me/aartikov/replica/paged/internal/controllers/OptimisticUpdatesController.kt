package me.aartikov.replica.paged.internal.controllers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import me.aartikov.replica.common.OptimisticUpdate
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedData
import me.aartikov.replica.paged.PagedReplicaState
import me.aartikov.replica.time.TimeProvider

internal class OptimisticUpdatesController<T : Any, P : Page<T>>(
    private val timeProvider: TimeProvider,
    private val dispatcher: CoroutineDispatcher,
    private val replicaStateFlow: MutableStateFlow<PagedReplicaState<T, P>>
) {

    suspend fun beginOptimisticUpdate(update: OptimisticUpdate<List<P>>) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            if (state.data != null) {
                replicaStateFlow.value = state.copy(
                    data = state.data.copy(
                        optimisticUpdates = state.data.optimisticUpdates + update
                    )
                )
            }
        }
    }

    suspend fun commitOptimisticUpdate(update: OptimisticUpdate<List<P>>) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            if (state.data != null) {
                val newData = update.apply(state.data.value.pages)
                replicaStateFlow.value = state.copy(
                    data = state.data.copy(
                        value = PagedData(newData),
                        optimisticUpdates = state.data.optimisticUpdates - update,
                        changingTime = timeProvider.currentTime
                    )
                )
            }
        }
    }

    suspend fun rollbackOptimisticUpdate(update: OptimisticUpdate<List<P>>) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            if (state.data != null) {
                replicaStateFlow.value = state.copy(
                    data = state.data.copy(
                        optimisticUpdates = state.data.optimisticUpdates - update
                    )
                )
            }
        }
    }
}