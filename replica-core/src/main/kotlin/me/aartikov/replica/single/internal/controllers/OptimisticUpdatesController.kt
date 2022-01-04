package me.aartikov.replica.single.internal.controllers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import me.aartikov.replica.single.OptimisticUpdate
import me.aartikov.replica.single.ReplicaState

internal class OptimisticUpdatesController<T : Any>(
    private val dispatcher: CoroutineDispatcher,
    private val replicaStateFlow: MutableStateFlow<ReplicaState<T>>
) {

    suspend fun beginOptimisticUpdate(update: OptimisticUpdate<T>) {
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

    suspend fun commitOptimisticUpdate(update: OptimisticUpdate<T>) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            if (state.data != null) {
                replicaStateFlow.value = state.copy(
                    data = state.data.copy(
                        value = update.apply(state.data.value),
                        optimisticUpdates = state.data.optimisticUpdates - update
                    )
                )
            }
        }
    }

    suspend fun rollbackOptimisticUpdate(update: OptimisticUpdate<T>) {
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