package me.aartikov.replica.paged.internal.controllers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedReplicaEvent
import me.aartikov.replica.paged.PagedReplicaState

internal class FreshnessController<T : Any, P : Page<T>>(
    private val dispatcher: CoroutineDispatcher,
    private val replicaStateFlow: MutableStateFlow<PagedReplicaState<T, P>>,
    private val replicaEventFlow: MutableSharedFlow<PagedReplicaEvent<T, P>>
) {

    suspend fun invalidate() {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            if (state.data?.fresh == true) {
                replicaStateFlow.value = state.copy(
                    data = state.data.copy(fresh = false)
                )
                replicaEventFlow.emit(PagedReplicaEvent.FreshnessEvent.BecameStale)
            }
        }
    }

    suspend fun makeFresh() {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            if (state.data != null) {
                replicaStateFlow.value = state.copy(
                    data = state.data.copy(fresh = true)
                )
                replicaEventFlow.emit(PagedReplicaEvent.FreshnessEvent.Freshened)
            }
        }
    }
}