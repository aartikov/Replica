package me.aartikov.replica.single.internal

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import me.aartikov.replica.common.internal.Lock
import me.aartikov.replica.common.internal.withLock
import me.aartikov.replica.single.Fetcher
import me.aartikov.replica.single.Storage

internal class DataLoader<T : Any>(
    private val coroutineScope: CoroutineScope,
    private val storage: Storage<T>?,
    private val fetcher: Fetcher<T>
) {

    sealed interface Output<out T : Any> {
        object LoadingStarted : Output<Nothing>

        sealed interface StorageRead<out T : Any> : Output<T> {
            data class Data<out T : Any>(val data: T) : Output<T>
            object Empty : Output<Nothing>
        }

        sealed interface LoadingFinished<out T : Any> : Output<T> {
            data class Success<out T : Any>(val data: T) : LoadingFinished<T>
            object Canceled : LoadingFinished<Nothing>
            data class Error(val exception: Exception) : LoadingFinished<Nothing>
        }
    }

    private val _outputFlow = MutableSharedFlow<Output<T>>(extraBufferCapacity = 1000)
    val outputFlow: Flow<Output<T>> = _outputFlow.asSharedFlow()

    val lock = Lock()
    private var loadingJob: Job? = null

    fun load(loadingFromStorageRequired: Boolean) = lock.withLock {
        if (loadingJob?.isActive == true) return

        loadingJob = coroutineScope.launch {
            try {
                _outputFlow.emit(Output.LoadingStarted)

                if (storage != null && loadingFromStorageRequired) {
                    val storageData = storage.read()
                    ensureActive()

                    if (storageData != null) {
                        _outputFlow.emit(Output.StorageRead.Data(storageData))
                    } else {
                        _outputFlow.emit(Output.StorageRead.Empty)
                    }
                }

                val data = fetcher.fetch()
                ensureActive()

                storage?.write(data)
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