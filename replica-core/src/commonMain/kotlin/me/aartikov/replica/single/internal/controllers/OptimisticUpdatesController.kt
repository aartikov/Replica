package me.aartikov.replica.single.internal.controllers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import me.aartikov.replica.common.OptimisticUpdate
import me.aartikov.replica.single.ReplicaState
import me.aartikov.replica.single.Storage
import me.aartikov.replica.time.TimeProvider

internal class OptimisticUpdatesController<T : Any>(
    private val timeProvider: TimeProvider,
    private val dispatcher: CoroutineDispatcher,
    private val replicaStateFlow: MutableStateFlow<ReplicaState<T>>,
    private val storage: Storage<T>?
) {

    suspend fun beginOptimisticUpdate(update: OptimisticUpdate<T>, operationId: Any) {
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
                val newData = update.apply(state.data.value)
                replicaStateFlow.value = state.copy(
                    data = state.data.copy(
                        value = newData,
                        optimisticUpdates = state.data.optimisticUpdates - operationId,
                        changingTime = timeProvider.currentTime
                    )
                )
                storage?.write(newData)
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