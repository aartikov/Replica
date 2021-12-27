package me.aartikov.replica.single.internal

import kotlinx.coroutines.*
import me.aartikov.replica.single.Fetcher
import me.aartikov.replica.single.Storage
import me.aartikov.replica.single.internal.Action.LoadingAction
import me.aartikov.sesame.loop.EffectHandler
import kotlin.coroutines.cancellation.CancellationException

internal class LoadingEffectHandler<T : Any>(
    private val storage: Storage<T>?,
    private val fetcher: Fetcher<T>
) :
    EffectHandler<Effect<T>, Action<T>> {

    private var job: Job? = null

    override suspend fun handleEffect(effect: Effect<T>, actionConsumer: (Action<T>) -> Unit) {
        when (effect) {
            is Effect.Load -> loadData(effect.checkStorage, actionConsumer)
            is Effect.CancelLoading -> cancelLoading()
            else -> Unit
        }
    }

    private suspend fun loadData(checkStorage: Boolean, actionConsumer: (Action<T>) -> Unit) =
        coroutineScope {
            job?.cancel()
            job = launch {
                if (checkStorage) {
                    loadDataFromStorage(actionConsumer)
                }
                loadDataFromFetcher(actionConsumer)
            }
        }

    private suspend fun CoroutineScope.loadDataFromStorage(actionConsumer: (Action<T>) -> Unit) {
        if (storage == null) return
        try {
            val data = storage.read()
            if (isActive) {
                if (data != null) {
                    actionConsumer(LoadingAction.DataLoadedFromStorage(data))
                } else {
                    actionConsumer(LoadingAction.DataMissingInStorage)
                }
            } else {
                actionConsumer(LoadingAction.LoadingCanceled)
            }
        } catch (e: CancellationException) {
            actionConsumer(LoadingAction.LoadingCanceled)
            throw e
        } catch (e: Exception) {
            if (isActive) {
                actionConsumer(LoadingAction.LoadingError(e))
            } else {
                actionConsumer(LoadingAction.LoadingCanceled)
            }
        }
    }

    private suspend fun CoroutineScope.loadDataFromFetcher(actionConsumer: (Action<T>) -> Unit) {
        try {
            val data = fetcher.fetch()
            if (isActive) {
                actionConsumer(LoadingAction.DataLoadedFromFetcher(data))
            } else {
                actionConsumer(LoadingAction.LoadingCanceled)
            }
        } catch (e: CancellationException) {
            actionConsumer(LoadingAction.LoadingCanceled)
            throw e
        } catch (e: Exception) {
            if (isActive) {
                actionConsumer(LoadingAction.LoadingError(e))
            } else {
                actionConsumer(LoadingAction.LoadingCanceled)
            }
        }
    }

    private fun cancelLoading() {
        job?.cancel()
        job = null
    }
}