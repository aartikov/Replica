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

    suspend fun refresh(refreshCondition: RefreshCondition) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            when (refreshCondition) {
                RefreshCondition.Never -> Unit
                RefreshCondition.IfHasObservers -> if (state.observerCount > 0) {
                    refresh()
                }
                RefreshCondition.IfHasActiveObservers -> if (state.activeObserverCount > 0) {
                    refresh()
                }
                RefreshCondition.Always -> refresh()
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

                is DataLoader.Output.StorageRead.Data -> {
                    if (state.data == null) {
                        replicaStateFlow.value = state.copy(
                            data = ReplicaData(
                                value = output.data,
                                fresh = false
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
                            state.data.copy(value = output.data, fresh = true)
                        } else {
                            ReplicaData(value = output.data, fresh = true)
                        },
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
                        error = LoadingError(output.exception),
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