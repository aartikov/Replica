package me.aartikov.replica.single.internal.controllers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import me.aartikov.replica.single.ReplicaEvent
import me.aartikov.replica.single.ReplicaState

internal class ClearingController<T : Any>(
    private val dispatcher: CoroutineDispatcher,
    private val replicaStateFlow: MutableStateFlow<ReplicaState<T>>,
    private val replicaEventFlow: MutableSharedFlow<ReplicaEvent<T>>
) {

    suspend fun clear() {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            replicaStateFlow.value = state.copy(data = null, error = null)
            replicaEventFlow.emit(ReplicaEvent.ClearedEvent)
        }
    }

    suspend fun clearError() {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            replicaStateFlow.value = state.copy(error = null)
        }
    }
}