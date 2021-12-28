package me.aartikov.replica.single.internal

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import me.aartikov.replica.single.Fetcher

internal class DataLoader<T : Any>(
    private val coroutineScope: CoroutineScope,
    private val fetcher: Fetcher<T>
) {

    sealed interface Output<out T : Any> {
        object LoadingStarted : Output<Nothing>

        sealed interface LoadingFinished<out T : Any> : Output<T> {
            data class Success<out T : Any>(val data: T) : LoadingFinished<T>
            object Canceled : LoadingFinished<Nothing>
            data class Error(val exception: Exception) : LoadingFinished<Nothing>
        }
    }

    private val _outputFlow = MutableSharedFlow<Output<T>>()
    val outputFlow: Flow<Output<T>> = _outputFlow.asSharedFlow()

    private var loadingJob: Job? = null

    @Synchronized
    fun load() {
        if (loadingJob?.isActive == true) return

        loadingJob = coroutineScope.launch {
            try {
                _outputFlow.emit(Output.LoadingStarted)

                val data = fetcher.fetch()
                if (currentCoroutineContext().isActive) {
                    _outputFlow.emit(Output.LoadingFinished.Success(data))
                } else {
                    _outputFlow.emit(Output.LoadingFinished.Canceled)
                }

            } catch (e: CancellationException) {
                _outputFlow.emit(Output.LoadingFinished.Canceled)
                throw e
            } catch (e: Exception) {
                if (currentCoroutineContext().isActive) {
                    _outputFlow.emit(Output.LoadingFinished.Error(e))
                } else {
                    _outputFlow.emit(Output.LoadingFinished.Canceled)
                }
            }
        }
    }

    @Synchronized
    fun cancelLoading() {
        loadingJob?.cancel()
        loadingJob = null
    }
}