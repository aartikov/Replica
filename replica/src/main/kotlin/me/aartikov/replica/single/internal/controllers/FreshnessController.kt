package me.aartikov.replica.single.internal.controllers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import me.aartikov.replica.single.ReplicaEvent
import me.aartikov.replica.single.ReplicaState

internal class FreshnessController<T : Any>(
    private val dispatcher: CoroutineDispatcher,
    private val replicaStateFlow: MutableStateFlow<ReplicaState<T>>,
    private val replicaEventFlow: MutableSharedFlow<ReplicaEvent<T>>
) {

    suspend fun invalidate() {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            if (state.data?.fresh == true) {
                state.copy(data = state.data.copy(fresh = false))
                replicaEventFlow.emit(ReplicaEvent.FreshnessEvent.BecameStale)
            }
        }
    }

    suspend fun makeFresh() {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            if (state.data != null) {
                replicaStateFlow.value = state.copy(data = state.data.copy(fresh = true))
                replicaEventFlow.emit(ReplicaEvent.FreshnessEvent.Freshened)
            }
        }
    }
}