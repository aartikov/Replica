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
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedData
import me.aartikov.replica.paged.PagedFetcher

internal class DataLoader<I : Any, P : Page<I>>(
    private val coroutineScope: CoroutineScope,
    private val fetcher: PagedFetcher<I, P>
) {
    sealed interface Output<out I : Any, out P : Page<I>> {

        val reason: LoadingReason

        sealed interface LoadingFinished<out I : Any, out P : Page<I>> : Output<I, P> {
            data class Success<out I : Any, out P : Page<I>>(
                override val reason: LoadingReason,
                val page: P
            ) : LoadingFinished<I, P>

            data class Error(
                override val reason: LoadingReason,
                val exception: Exception
            ) : LoadingFinished<Nothing, Nothing>
        }
    }

    private val _outputFlow = MutableSharedFlow<Output<I, P>>(extraBufferCapacity = 1000)
    val outputFlow: Flow<Output<I, P>> = _outputFlow.asSharedFlow()

    private var loadingJob: Job? = null

    fun loadFirstPage() {
        load(LoadingReason.Normal) {
            fetcher.fetchFirstPage()
        }
    }

    fun loadNextPage(currentData: PagedData<I, P>) {
        load(LoadingReason.NextPage) {
            fetcher.fetchNextPage(currentData)
        }
    }

    fun loadPreviousPage(currentData: PagedData<I, P>) {
        load(LoadingReason.PreviousPage) {
            fetcher.fetchPreviousPage(currentData)
        }
    }

    private fun load(reason: LoadingReason, fetchMethod: suspend () -> P) {
        loadingJob = coroutineScope.launch {
            try {
                val data = fetchMethod()
                ensureActive()
                _outputFlow.emit(Output.LoadingFinished.Success(reason, data))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                withContext(NonCancellable) {
                    if (currentCoroutineContext().isActive) {
                        _outputFlow.emit(Output.LoadingFinished.Error(reason, e))
                    }
                }
            }
        }
    }

    fun cancel() {
        loadingJob?.cancel()
        loadingJob = null
    }
}