package me.aartikov.replica.single.internal.controllers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import me.aartikov.replica.single.ReplicaData
import me.aartikov.replica.single.ReplicaState

internal class DataChangingController<T : Any>(
    private val dispatcher: CoroutineDispatcher,
    private val replicaStateFlow: MutableStateFlow<ReplicaState<T>>
) {

    suspend fun setData(data: T) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            replicaStateFlow.value = state.copy(
                data = if (state.data != null) {
                    state.data.copy(value = data)
                } else {
                    ReplicaData(value = data, fresh = false)
                }
            )
        }
    }

    suspend fun mutateData(transform: (T) -> T) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            if (state.data != null) {
                replicaStateFlow.value = state.copy(
                    data = state.data.copy(value = transform(state.data.value))
                )
            }
        }
    }
}