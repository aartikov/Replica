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
import me.aartikov.replica.common.internal.Lock
import me.aartikov.replica.common.internal.withLock
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedFetcher

internal class DataLoader<T : Any, P : Page<T>>(
    private val coroutineScope: CoroutineScope,
    private val fetcher: PagedFetcher<T, P>
) {

    sealed interface Output<out T : Any> {
        data object LoadingStarted : Output<Nothing>

        sealed interface LoadingFinished<out T : Any> : Output<T> {
            data class Success<out T : Any>(val data: T) : LoadingFinished<T>
            data object Canceled : LoadingFinished<Nothing>
            data class Error(val exception: Exception) : LoadingFinished<Nothing>
        }
    }

    private val _outputFlow = MutableSharedFlow<Output<T>>(extraBufferCapacity = 1000)
    val outputFlow: Flow<Output<T>> = _outputFlow.asSharedFlow()

    val lock = Lock()
    private var loadingJob: Job? = null

    fun loadFirstPage() {

    }

    fun loadNextPage() {

    }

    fun loadPreviousPage() {

    }

    private fun load(block: () -> P?) = lock.withLock {
        if (loadingJob?.isActive == true) return

        loadingJob = coroutineScope.launch {
            try {
                _outputFlow.emit(Output.LoadingStarted)

                val data = fetcher.fetch()
                ensureActive()

                _outputFlow.emit(Output.LoadingFinished.Success(data))

            } catch (e: CancellationException) {
                withContext(NonCancellable) {
                    _outputFlow.emit(Output.LoadingFinished.Canceled)
                }
                throw e
            } catch (e: Exception) {
                withContext(NonCancellable) {
                    if (currentCoroutineContext().isActive) {
                        _outputFlow.emit(Output.LoadingFinished.Error(e))
                    } else {
                        _outputFlow.emit(Output.LoadingFinished.Canceled)
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