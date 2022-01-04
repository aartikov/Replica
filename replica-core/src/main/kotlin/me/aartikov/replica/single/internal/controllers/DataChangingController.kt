package me.aartikov.replica.single.internal.controllers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import me.aartikov.replica.single.ReplicaData
import me.aartikov.replica.single.ReplicaState
import me.aartikov.replica.single.Storage

internal class DataChangingController<T : Any>(
    private val dispatcher: CoroutineDispatcher,
    private val replicaStateFlow: MutableStateFlow<ReplicaState<T>>,
    private val storage: Storage<T>?
) {

    suspend fun setData(data: T) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            replicaStateFlow.value = state.copy(
                data = if (state.data != null) {
                    state.data.copy(value = data)
                } else {
                    ReplicaData(value = data, fresh = false)
                },
                loadingFromStorageRequired = false
            )
            storage?.write(data)
        }
    }

    suspend fun mutateData(transform: (T) -> T) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            if (state.data != null) {
                val newData = transform(state.data.value)
                replicaStateFlow.value = state.copy(
                    data = state.data.copy(value = newData),
                    loadingFromStorageRequired = false
                )
                storage?.write(newData)
            }
        }
    }
}