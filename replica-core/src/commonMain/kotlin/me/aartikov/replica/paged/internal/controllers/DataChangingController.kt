package me.aartikov.replica.paged.internal.controllers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedData
import me.aartikov.replica.paged.PagedReplicaData
import me.aartikov.replica.paged.PagedReplicaState
import me.aartikov.replica.time.TimeProvider

internal class DataChangingController<T : Any, P : Page<T>>(
    private val timeProvider: TimeProvider,
    private val dispatcher: CoroutineDispatcher,
    private val idExtractor: ((T) -> Any)?,
    private val replicaStateFlow: MutableStateFlow<PagedReplicaState<T, P>>,
) {

    suspend fun setData(data: List<P>) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            replicaStateFlow.value = state.copy(
                data = if (state.data != null) {
                    state.data.copy(
                        value = PagedData(data, idExtractor),
                        changingTime = timeProvider.currentTime
                    )
                } else {
                    PagedReplicaData(
                        value = PagedData(data, idExtractor),
                        fresh = false,
                        changingTime = timeProvider.currentTime,
                        idExtractor = idExtractor
                    )
                }
            )
        }
    }

    suspend fun mutateData(transform: (List<P>) -> List<P>) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            if (state.data != null) {
                val newData = transform(state.data.value.pages)
                replicaStateFlow.value = state.copy(
                    data = state.data.copy(
                        value = PagedData(newData, idExtractor),
                        changingTime = timeProvider.currentTime
                    )
                )
            }
        }
    }
}