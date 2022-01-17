package me.aartikov.replica.single.internal.controllers

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import me.aartikov.replica.single.*
import me.aartikov.replica.single.internal.DataLoader

internal class DataLoadingController<T : Any>(
    private val dispatcher: CoroutineDispatcher,
    private val coroutineScope: CoroutineScope,
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
        dataLoader.load(replicaStateFlow.value.loadingFromStorageRequired)
    }

    suspend fun refreshAfterInvalidation(invalidationMode: InvalidationMode) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            when (invalidationMode) {
                InvalidationMode.DontRefresh -> Unit
                InvalidationMode.RefreshIfHasObservers -> if (state.observingStatus != ObservingStatus.None) {
                    refresh()
                }
                InvalidationMode.RefreshIfHasActiveObservers -> if (state.observingStatus == ObservingStatus.Active) {
                    refresh()
                }
                InvalidationMode.RefreshAlways -> refresh()
            }
        }
    }

    fun revalidate() {
        coroutineScope.launch { // launch and dispatcher are required to get replicaState without race conditions
            withContext(dispatcher) {
                if (!replicaStateFlow.value.hasFreshData) {
                    dataLoader.load(replicaStateFlow.value.loadingFromStorageRequired)
                }
            }
        }
    }

    suspend fun getData(): T {
        return getDataInternal(refreshed = false)
    }

    suspend fun getRefreshedData(): T {
        return getDataInternal(refreshed = true)
    }

    fun cancel() {
        dataLoader.cancel()
    }

    private suspend fun onDataLoaderOutput(output: DataLoader.Output<T>) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            when (output) {
                is DataLoader.Output.LoadingStarted -> {
                    replicaStateFlow.value = state.copy(
                        loading = true,
                        preloading = state.observingStatus == ObservingStatus.None
                    )
                    replicaEventFlow.emit(ReplicaEvent.LoadingEvent.LoadingStarted)
                }

                is DataLoader.Output.StorageRead.Data -> {
                    if (state.data == null) {
                        replicaStateFlow.value = state.copy(
                            data = ReplicaData(
                                value = output.data,
                                freshness = Freshness.Stale
                            ),
                            loadingFromStorageRequired = false
                        )
                        replicaEventFlow.emit(ReplicaEvent.LoadingEvent.DataFromStorageLoaded(output.data))
                    }
                }

                is DataLoader.Output.StorageRead.Empty -> {
                    replicaStateFlow.value = state.copy(loadingFromStorageRequired = false)
                }

                is DataLoader.Output.LoadingFinished.Success -> {
                    replicaStateFlow.value = state.copy(
                        data = if (state.data != null) {
                            state.data.copy(value = output.data, freshness = Freshness.Fresh)
                        } else {
                            ReplicaData(value = output.data, freshness = Freshness.Fresh)
                        },
                        error = null,
                        loading = false,
                        preloading = false,
                        dataRequested = false
                    )
                    replicaEventFlow.emit(ReplicaEvent.LoadingEvent.LoadingFinished.Success(output.data))
                    replicaEventFlow.emit(ReplicaEvent.FreshnessEvent.Freshened)
                }

                DataLoader.Output.LoadingFinished.Canceled -> {
                    replicaStateFlow.value = state.copy(
                        loading = false,
                        preloading = false,
                        dataRequested = false
                    )
                    replicaEventFlow.emit(ReplicaEvent.LoadingEvent.LoadingFinished.Canceled)
                }

                is DataLoader.Output.LoadingFinished.Error -> {
                    replicaStateFlow.value = state.copy(
                        error = LoadingError(output.exception),
                        loading = false,
                        preloading = false,
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
            if (!refreshed && data?.fresh == true) {
                return@withContext data.valueWithOptimisticUpdates
            }

            val output = dataLoader.outputFlow
                .onStart {
                    dataLoader.load(replicaStateFlow.value.loadingFromStorageRequired)
                    val state = replicaStateFlow.value
                    if (!state.dataRequested) {
                        replicaStateFlow.value = state.copy(dataRequested = true)
                    }
                }
                .filterIsInstance<DataLoader.Output.LoadingFinished<T>>()
                .first()



            when (output) {
                is DataLoader.Output.LoadingFinished.Success -> {
                    val optimisticUpdates = replicaStateFlow.value.data?.optimisticUpdates
                    optimisticUpdates?.applyAll(output.data) ?: output.data
                }
                is DataLoader.Output.LoadingFinished.Canceled -> throw CancellationException()
                is DataLoader.Output.LoadingFinished.Error -> throw output.exception
            }
        }
    }
}