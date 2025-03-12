package me.aartikov.replica.paged.internal.controllers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

internal class DataLoadingController<I : Any, P : Page<I>>(
    coroutineScope: CoroutineScope,
    private val timeProvider: TimeProvider,
    private val dispatcher: CoroutineDispatcher,
    private val idExtractor: ((I) -> Any)?,
    private val replicaStateFlow: MutableStateFlow<PagedReplicaState<I, P>>,
    private val replicaEventFlow: MutableSharedFlow<PagedReplicaEvent<I, P>>,
    private val dataLoader: DataLoader<I, P>
) {

    init {
        dataLoader.outputFlow
            .onEach(::onDataLoaderOutput)
            .launchIn(coroutineScope)
    }

    suspend fun refresh() {
        loadFirstPage(skipLoadingIfFresh = false)
    }

    suspend fun revalidate() {
        loadFirstPage(skipLoadingIfFresh = true)
    }

    suspend fun loadNext() {
        loadNextOrPreviousPage(isNext = true)
    }

    suspend fun loadPrevious() {
        loadNextOrPreviousPage(isNext = false)
    }

    suspend fun cancel() {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            if (state.loadingStatus == PagedLoadingStatus.None) return@withContext

            dataLoader.cancel()

            replicaStateFlow.value = state.copy(
                loadingStatus = PagedLoadingStatus.None,
                preloading = false
            )

            val reason = when (state.loadingStatus) {
                PagedLoadingStatus.None -> return@withContext
                PagedLoadingStatus.LoadingFirstPage -> LoadingReason.Normal
                PagedLoadingStatus.LoadingNextPage -> LoadingReason.NextPage
                PagedLoadingStatus.LoadingPreviousPage -> LoadingReason.PreviousPage
            }

            replicaEventFlow.emit(
                PagedReplicaEvent.LoadingEvent.LoadingFinished.Canceled(reason)
            )
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

    private suspend fun loadFirstPage(skipLoadingIfFresh: Boolean) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value

            if (skipLoadingIfFresh && state.hasFreshData) return@withContext

            val currentLoadingStatus = replicaStateFlow.value.loadingStatus
            val loadingStarted = when (currentLoadingStatus) {
                PagedLoadingStatus.LoadingFirstPage -> false
                else -> {
                    dataLoader.cancel()
                    dataLoader.loadFirstPage()
                    true
                }
            }

            val preloading = state.observingState.status == ObservingStatus.None

            replicaStateFlow.value = state.copy(
                loadingStatus = PagedLoadingStatus.LoadingFirstPage,
                preloading = preloading || state.preloading
            )

            if (loadingStarted) {
                replicaEventFlow.emit(
                    PagedReplicaEvent.LoadingEvent.LoadingStarted(LoadingReason.Normal)
                )
            }
        }
    }

    private suspend fun loadNextOrPreviousPage(isNext: Boolean) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            val currentData = replicaStateFlow.value.data ?: return@withContext

            val currentLoadingStatus = replicaStateFlow.value.loadingStatus

            val requiredLoadingStatus = when (isNext) {
                true -> PagedLoadingStatus.LoadingNextPage
                false -> PagedLoadingStatus.LoadingPreviousPage
            }

            val loadingStarted = when (currentLoadingStatus) {
                PagedLoadingStatus.LoadingFirstPage, requiredLoadingStatus -> false
                else -> {
                    dataLoader.cancel()
                    val loadingOperation = when (isNext) {
                        true -> dataLoader::loadNextPage
                        false -> dataLoader::loadPreviousPage
                    }
                    loadingOperation(currentData.valueWithOptimisticUpdates)
                    true
                }
            }

            val preloading = state.observingState.status == ObservingStatus.None

            replicaStateFlow.value = state.copy(
                loadingStatus = requiredLoadingStatus,
                preloading = preloading || state.preloading
            )

            if (loadingStarted) {
                val loadingReason = when (isNext) {
                    true -> LoadingReason.NextPage
                    false -> LoadingReason.PreviousPage
                }
                replicaEventFlow.emit(
                    PagedReplicaEvent.LoadingEvent.LoadingStarted(loadingReason)
                )
            }
        }
    }

    private suspend fun onDataLoaderOutput(output: DataLoader.Output<I, P>) {
        withContext(dispatcher) {
            val state = replicaStateFlow.value
            when (output) {

                is DataLoader.Output.LoadingFinished.Success -> {
                    val newData = if (state.data != null) {
                        val newPages = when (output.reason) {
                            LoadingReason.Normal -> listOf(output.page)
                            LoadingReason.NextPage -> state.data.value.pages + listOf(output.page)
                            LoadingReason.PreviousPage -> listOf(output.page) + state.data.value.pages
                        }
                        PagedData(newPages, idExtractor)
                    } else {
                        PagedData(listOf(output.page), idExtractor)
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
                                changingTime = timeProvider.currentTime,
                                idExtractor = idExtractor
                            )
                        },
                        error = null,
                        loadingStatus = PagedLoadingStatus.None,
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

                is DataLoader.Output.LoadingFinished.Error -> {
                    replicaStateFlow.value = state.copy(
                        error = LoadingError(output.reason, output.exception),
                        loadingStatus = PagedLoadingStatus.None,
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