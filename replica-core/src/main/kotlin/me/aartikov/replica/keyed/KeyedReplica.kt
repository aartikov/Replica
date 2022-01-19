package me.aartikov.replica.keyed

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.common.LoadingError
import me.aartikov.replica.keyed.internal.keepPreviousData
import me.aartikov.replica.single.Loadable
import me.aartikov.replica.single.ReplicaObserver
import me.aartikov.replica.single.currentState

interface KeyedReplica<K : Any, out T : Any> {

    fun observe(
        observerCoroutineScope: CoroutineScope,
        observerActive: StateFlow<Boolean>,
        key: StateFlow<K?>
    ): ReplicaObserver<T>

    fun refresh(key: K)

    fun revalidate(key: K)

    suspend fun getData(key: K): T

    suspend fun getRefreshedData(key: K): T
}

fun <K : Any, T : Any> KeyedReplica<K, T>.observe(
    observerCoroutineScope: CoroutineScope,
    observerActive: StateFlow<Boolean>,
    key: StateFlow<K?>,
    onError: (LoadingError, Loadable<T>) -> Unit,
    keepPreviousData: Boolean = false
): StateFlow<Loadable<T>> {
    val observer = observe(observerCoroutineScope, observerActive, key)
    observer
        .loadingErrorFlow
        .onEach { error ->
            onError(error, observer.currentState)
        }
        .launchIn(observerCoroutineScope)

    return if (keepPreviousData) {
        observer.stateFlow.keepPreviousData(observerCoroutineScope)
    } else {
        observer.stateFlow
    }
}