package me.aartikov.replica.paged.internal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.aartikov.replica.common.LoadingReason
import me.aartikov.replica.common.internal.Lock
import me.aartikov.replica.common.internal.withLock
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedData
import me.aartikov.replica.paged.PagedFetcher

internal class DataLoader<T : Any, P : Page<T>>(
    private val coroutineScope: CoroutineScope,
    private val fetcher: PagedFetcher<T, P>
) {
    sealed interface Output<out T : Any, out P : Page<T>> {

        val reason: LoadingReason

        data class LoadingStarted(
            override val reason: LoadingReason
        ) : Output<Nothing, Nothing>

        sealed interface LoadingFinished<out T : Any, out P : Page<T>> : Output<T, P> {

            data class Success<out T : Any, out P : Page<T>>(
                override val reason: LoadingReason,
                val page: P
            ) : LoadingFinished<T, P>

            data class Canceled(
                override val reason: LoadingReason
            ) : LoadingFinished<Nothing, Nothing>

            data class Error(
                override val reason: LoadingReason,
                val exception: Exception
            ) : LoadingFinished<Nothing, Nothing>
        }
    }

    private val _outputFlow = MutableSharedFlow<Output<T, P>>(extraBufferCapacity = 1000)
    val outputFlow: Flow<Output<T, P>> = _outputFlow.asSharedFlow()

    private val lock = Lock()
    private var loadingJob: Job? = null

    fun loadFirstPage(cancel: Boolean) {
        load(cancel, LoadingReason.Normal) {
            fetcher.fetchFirstPage()
        }
    }

    fun loadNextPage(cancel: Boolean, currentData: PagedData<T, P>) {
        load(cancel, LoadingReason.NextPage) {
            fetcher.fetchNextPage(currentData)
        }
    }

    fun loadPreviousPage(cancel: Boolean, currentData: PagedData<T, P>) {
        load(cancel, LoadingReason.PreviousPage) {
            fetcher.fetchPreviousPage(currentData)
        }
    }

    private fun load(cancel: Boolean, reason: LoadingReason, fetchMethod: suspend () -> P) = lock
        .withLock {
            if (cancel) {
                loadingJob?.cancel()
            } else if (loadingJob?.isActive == true) {
                return@withLock
            }

            loadingJob = coroutineScope.launch {
                try {
                    _outputFlow.emit(Output.LoadingStarted(reason))

                    val data = fetchMethod()
                    ensureActive()

                    _outputFlow.emit(Output.LoadingFinished.Success(reason, data))

                } catch (e: CancellationException) {
                    withContext(NonCancellable) {
                        _outputFlow.emit(Output.LoadingFinished.Canceled(reason))
                    }
                    throw e
                } catch (e: Exception) {
                    withContext(NonCancellable) {
                        if (currentCoroutineContext().isActive) {
                            _outputFlow.emit(Output.LoadingFinished.Error(reason, e))
                        } else {
                            _outputFlow.emit(Output.LoadingFinished.Canceled(reason))
                        }
                    }
                }
            }
        }

    fun cancel() = lock.withLock {
        loadingJob?.cancel()
        loadingJob = null
    }
}