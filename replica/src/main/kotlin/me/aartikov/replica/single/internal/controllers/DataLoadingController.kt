package me.aartikov.replica.single.internal.controllers

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import me.aartikov.replica.single.ReplicaData
import me.aartikov.replica.single.ReplicaEvent
import me.aartikov.replica.single.ReplicaState
import me.aartikov.replica.single.internal.DataLoader

internal class DataLoadingController<T : Any>(
    private val dispatcher: CoroutineDispatcher,
    coroutineScope: CoroutineScope,
    private val replicaStateFlow: MutableStateFlow<ReplicaState<T>>,
    private val replicaEventFlow: MutableSharedFlow<ReplicaEvent<T>>,
    private val dataLoader: DataLoader<T>
) {

    init {
        dataLoader.outputFlow
            .onEach(::onDataLoaderOutput)
            .launchIn(coroutineScope)
    }


    fun refresh() {
        dataLoader.load()
    }

    fun revalidate() {
        if (!replicaStateFlow.value.hasFreshData) {
            dataLoader.load()
        }
    }

    suspend fun getData(): T {
        return getDataInternal(refreshed = false)
    }

    suspend fun getRefreshedData(): T {
        return getDataInternal(refreshed = true)
    }

    fun cancelLoading() {
        dataLoader.cancelLoading()
    }

    private suspend fun onDataLoaderOutput(output: DataLoader.Output<T>) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            when (output) {
                is DataLoader.Output.LoadingStarted -> {
                    replicaStateFlow.value = state.copy(loading = true)
                    replicaEventFlow.emit(ReplicaEvent.LoadingEvent.LoadingStarted)
                }

                is DataLoader.Output.LoadingFinished.Success -> {
                    replicaStateFlow.value = state.copy(
                        data = ReplicaData(
                            value = output.data,
                            fresh = true
                        ),
                        error = null,
                        loading = false,
                        dataRequested = false
                    )
                    replicaEventFlow.emit(ReplicaEvent.LoadingEvent.LoadingFinished.Success(output.data))
                    replicaEventFlow.emit(ReplicaEvent.FreshnessEvent.Freshened)
                }

                DataLoader.Output.LoadingFinished.Canceled -> {
                    replicaStateFlow.value = state.copy(
                        loading = false,
                        dataRequested = false
                    )
                    replicaEventFlow.emit(ReplicaEvent.LoadingEvent.LoadingFinished.Canceled)
                }

                is DataLoader.Output.LoadingFinished.Error -> {
                    replicaStateFlow.value = state.copy(
                        error = output.exception,
                        loading = false,
                        dataRequested = false
                    )
                    replicaEventFlow.emit(ReplicaEvent.LoadingEvent.LoadingFinished.Error(output.exception))
                }
            }
        }
    }

    private suspend fun getDataInternal(refreshed: Boolean): T {
        return withContext(dispatcher) {
            val data = replicaStateFlow.value.data
            if (!refreshed && data != null && data.fresh) {
                return@withContext data.value
            }

            val output = dataLoader.outputFlow
                .onStart {
                    dataLoader.load()
                    val state = replicaStateFlow.value
                    if (!state.dataRequested) {
                        replicaStateFlow.value = state.copy(dataRequested = true)
                    }
                }
                .filterIsInstance<DataLoader.Output.LoadingFinished<T>>()
                .first()

            when (output) {
                is DataLoader.Output.LoadingFinished.Success -> output.data
                is DataLoader.Output.LoadingFinished.Canceled -> throw CancellationException()
                is DataLoader.Output.LoadingFinished.Error -> throw output.exception
            }
        }
    }
}