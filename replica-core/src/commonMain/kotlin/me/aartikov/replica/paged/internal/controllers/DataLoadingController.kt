package me.aartikov.replica.paged.internal.controllers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.aartikov.replica.common.InvalidationMode
import me.aartikov.replica.common.LoadingError
import me.aartikov.replica.common.LoadingReason
import me.aartikov.replica.common.ObservingStatus
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedData
import me.aartikov.replica.paged.PagedLoadingStatus
import me.aartikov.replica.paged.PagedReplicaData
import me.aartikov.replica.paged.PagedReplicaEvent
import me.aartikov.replica.paged.PagedReplicaState
import me.aartikov.replica.paged.internal.DataLoader
import me.aartikov.replica.time.TimeProvider

internal class DataLoadingController<T : Any, P : Page<T>>(
    private val timeProvider: TimeProvider,
    private val dispatcher: CoroutineDispatcher,
    private val coroutineScope: CoroutineScope,
    private val replicaStateFlow: MutableStateFlow<PagedReplicaState<T, P>>,
    private val replicaEventFlow: MutableSharedFlow<PagedReplicaEvent<T, P>>,
    private val dataLoader: DataLoader<T, P>
) {

    init {
        dataLoader.outputFlow
            .onEach(::onDataLoaderOutput)
            .launchIn(coroutineScope)
    }

    fun refresh() {
        coroutineScope.launch { // launch and dispatcher are required to get replicaState without race conditions
            withContext(dispatcher) {
                refreshImpl()
            }
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

    fun revalidate() {
        coroutineScope.launch { // launch and dispatcher are required to get replicaState without race conditions
            withContext(dispatcher) {
                if (!replicaStateFlow.value.hasFreshData) {
                    refreshImpl()
                }
            }
        }
    }

    fun cancel() {
        dataLoader.cancel()
    }

    private fun refreshImpl() {
        val currentLoadingStatus = replicaStateFlow.value.loading
        val cancel = currentLoadingStatus != PagedLoadingStatus.LoadingFirstPage
        dataLoader.loadFirstPage(cancel)
    }

    private suspend fun onDataLoaderOutput(output: DataLoader.Output<T, P>) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            when (output) {
                is DataLoader.Output.LoadingStarted -> {
                    replicaStateFlow.value = state.copy(
                        loading = output.reason.toLoadingStatus(),
                        preloading = state.observingState.status == ObservingStatus.None
                    )
                    replicaEventFlow.emit(
                        PagedReplicaEvent.LoadingEvent.LoadingStarted(output.reason)
                    )
                }

                is DataLoader.Output.LoadingFinished.Success -> {
                    val newData = if (state.data != null) {
                        PagedData(listOf(output.page)) // to do merge pages
                    } else {
                        PagedData(listOf(output.page))
                    }

                    replicaStateFlow.value = state.copy(
                        data = if (state.data != null) {
                            state.data.copy(
                                value = newData,
                                fresh = true,
                                changingTime = timeProvider.currentTime
                            )
                        } else {
                            PagedReplicaData(
                                value = newData,
                                fresh = true,
                                changingTime = timeProvider.currentTime
                            )
                        },
                        error = null,
                        loading = PagedLoadingStatus.None,
                        preloading = false
                    )
                    replicaEventFlow.emit(
                        PagedReplicaEvent.LoadingEvent.LoadingFinished.Success(
                            output.reason,
                            output.page
                        )
                    )
                    replicaEventFlow.emit(PagedReplicaEvent.FreshnessEvent.Freshened)
                }

                is DataLoader.Output.LoadingFinished.Canceled -> {
                    replicaStateFlow.value = state.copy(
                        loading = PagedLoadingStatus.None,
                        preloading = false
                    )
                    replicaEventFlow.emit(
                        PagedReplicaEvent.LoadingEvent.LoadingFinished.Canceled(output.reason)
                    )
                }

                is DataLoader.Output.LoadingFinished.Error -> {
                    replicaStateFlow.value = state.copy(
                        error = LoadingError(output.reason, output.exception),
                        loading = PagedLoadingStatus.None,
                        preloading = false
                    )
                    replicaEventFlow.emit(
                        PagedReplicaEvent.LoadingEvent.LoadingFinished.Error(
                            output.reason,
                            output.exception
                        )
                    )
                }
            }
        }
    }
}

private fun LoadingReason.toLoadingStatus(): PagedLoadingStatus = when (this) {
    LoadingReason.Normal -> PagedLoadingStatus.LoadingFirstPage
    LoadingReason.NextPage -> PagedLoadingStatus.LoadingNextPage
    LoadingReason.PreviousPage -> PagedLoadingStatus.LoadingPreviousPage
}