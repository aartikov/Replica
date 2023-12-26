package me.aartikov.replica.paged.internal.controllers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedReplicaEvent
import me.aartikov.replica.paged.PagedReplicaState

internal class ClearingController<T : Any, P : Page<T>>(
    private val dispatcher: CoroutineDispatcher,
    private val replicaStateFlow: MutableStateFlow<PagedReplicaState<T, P>>,
    private val replicaEventFlow: MutableSharedFlow<PagedReplicaEvent<T, P>>,
) {

    suspend fun clear() {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            replicaStateFlow.value = state.copy(data = null, error = null)
            replicaEventFlow.emit(PagedReplicaEvent.ClearedEvent)
        }
    }

    suspend fun clearError() {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            replicaStateFlow.value = state.copy(error = null)
        }
    }
}