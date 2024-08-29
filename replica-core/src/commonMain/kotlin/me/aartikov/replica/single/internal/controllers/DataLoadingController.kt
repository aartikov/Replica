package me.aartikov.replica.single.internal.controllers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import me.aartikov.replica.common.InvalidationMode
import me.aartikov.replica.common.LoadingError
import me.aartikov.replica.common.LoadingReason
import me.aartikov.replica.common.ObservingStatus
import me.aartikov.replica.common.applyAll
import me.aartikov.replica.single.ReplicaData
import me.aartikov.replica.single.ReplicaEvent
import me.aartikov.replica.single.ReplicaState
import me.aartikov.replica.single.internal.DataLoader
import me.aartikov.replica.time.TimeProvider

internal class DataLoadingController<T : Any>(
    private val timeProvider: TimeProvider,
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


    suspend fun refresh() {
        loadData(dontLoadIfFresh = false)
    }

    suspend fun revalidate() {
        loadData(dontLoadIfFresh = true)
    }

    suspend fun cancel() {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            if (!state.loading) return@withContext

            dataLoader.cancel()

            replicaStateFlow.value = state.copy(
                loading = false,
                preloading = false,
                dataRequested = false
            )

            replicaEventFlow.emit(ReplicaEvent.LoadingEvent.LoadingFinished.Canceled)
        }
    }

    suspend fun refreshAfterInvalidation(invalidationMode: InvalidationMode) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            when (invalidationMode) {
                InvalidationMode.DontRefresh -> Unit

                InvalidationMode.RefreshIfHasObservers -> {
                    if (state.observingState.status != ObservingStatus.None) refresh()
                }

                InvalidationMode.RefreshIfHasActiveObservers -> {
                    if (state.observingState.status == ObservingStatus.Active) refresh()
                }

                InvalidationMode.RefreshAlways -> refresh()
            }
        }
    }

    suspend fun getData(forceRefresh: Boolean): T {
        return withContext(dispatcher) {
            val data = replicaStateFlow.value.data
            if (!forceRefresh && data?.fresh == true) {
                return@withContext data.valueWithOptimisticUpdates
            }

            val output = dataLoader.outputFlow
                .onStart {
                    loadData(dontLoadIfFresh = false, setDataRequested = true)
                }
                .filterIsInstance<DataLoader.Output.LoadingFinished<T>>()
                .first()

            when (output) {
                is DataLoader.Output.LoadingFinished.Success -> {
                    val optimisticUpdates = replicaStateFlow.value.data?.optimisticUpdates
                    optimisticUpdates?.applyAll(output.data) ?: output.data
                }

                is DataLoader.Output.LoadingFinished.Error -> throw output.exception
            }
        }
    }

    private suspend fun loadData(
        dontLoadIfFresh: Boolean,
        setDataRequested: Boolean = false
    ) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            if (dontLoadIfFresh && state.hasFreshData) return@withContext

            val loadingStarted = if (!state.loading) {
                dataLoader.load(state.loadingFromStorageRequired)
                true
            } else {
                false
            }

            replicaStateFlow.value = state.copy(
                loading = true,
                preloading = state.observingState.status == ObservingStatus.None,
                dataRequested = if (setDataRequested) true else state.dataRequested
            )

            if (loadingStarted) {
                replicaEventFlow.emit(ReplicaEvent.LoadingEvent.LoadingStarted)
            }
        }
    }

    private suspend fun onDataLoaderOutput(output: DataLoader.Output<T>) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            when (output) {

                is DataLoader.Output.StorageRead.Data -> {
                    if (state.data == null) {
                        replicaStateFlow.value = state.copy(
                            data = ReplicaData(
                                value = output.data,
                                fresh = false,
                                changingTime = timeProvider.currentTime
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
                        data = ReplicaData(
                            value = output.data,
                            fresh = true,
                            changingTime = timeProvider.currentTime,
                            optimisticUpdates = state.data?.optimisticUpdates ?: emptyList()
                        ),
                        error = null,
                        loading = false,
                        preloading = false,
                        dataRequested = false
                    )
                    replicaEventFlow.emit(ReplicaEvent.LoadingEvent.LoadingFinished.Success(output.data))
                    replicaEventFlow.emit(ReplicaEvent.FreshnessEvent.Freshened)
                }

                is DataLoader.Output.LoadingFinished.Error -> {
                    replicaStateFlow.value = state.copy(
                        error = LoadingError(LoadingReason.Normal, output.exception),
                        loading = false,
                        preloading = false,
                        dataRequested = false
                    )
                    replicaEventFlow.emit(ReplicaEvent.LoadingEvent.LoadingFinished.Error(output.exception))
                }
            }
        }
    }
}