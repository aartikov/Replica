package me.aartikov.replica.single

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.common.LoadingError

interface Replica<out T : Any> {

    fun observe(
        observerCoroutineScope: CoroutineScope,
        observerActive: StateFlow<Boolean>
    ): ReplicaObserver<T>

    fun refresh()

    fun revalidate()

    suspend fun getData(): T

    suspend fun getRefreshedData(): T

}

fun <T : Any> Replica<T>.observe(
    observerCoroutineScope: CoroutineScope,
    observerActive: StateFlow<Boolean>,
    onError: (LoadingError, Loadable<T>) -> Unit
): StateFlow<Loadable<T>> {
    val observer = observe(observerCoroutineScope, observerActive)
    observer
        .loadingErrorFlow
        .onEach { onError(it, observer.currentState) }
        .launchIn(observerCoroutineScope)

    return observer.stateFlow
}