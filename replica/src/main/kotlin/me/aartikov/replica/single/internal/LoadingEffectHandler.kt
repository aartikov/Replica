package me.aartikov.replica.single.internal

import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.aartikov.replica.single.Fetcher
import me.aartikov.replica.single.internal.Action.LoadingAction
import me.aartikov.sesame.loop.EffectHandler
import kotlin.coroutines.cancellation.CancellationException

internal class LoadingEffectHandler<T : Any>(private val fetcher: Fetcher<T>) :
    EffectHandler<Effect<T>, Action<T>> {

    private var job: Job? = null

    override suspend fun handleEffect(effect: Effect<T>, actionConsumer: (Action<T>) -> Unit) {
        when (effect) {
            is Effect.Load -> loadData(actionConsumer)
            is Effect.CancelLoading -> cancelLoading()
            else -> Unit
        }
    }

    private suspend fun loadData(actionConsumer: (Action<T>) -> Unit) = coroutineScope {
        job?.cancel()
        job = launch {
            try {
                val data = fetcher.fetch()
                if (isActive) {
                    actionConsumer(LoadingAction.DataLoaded(data))
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
    }

    private fun cancelLoading() {
        job?.cancel()
        job = null
    }
}