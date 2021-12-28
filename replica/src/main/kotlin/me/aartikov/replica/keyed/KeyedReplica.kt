package me.aartikov.replica.keyed

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.keyed.internal.keepPreviousData
import me.aartikov.replica.single.*

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

fun <K : Any, T : Any> KeyedReplica<K, T>.observe(
    observerCoroutineScope: CoroutineScope,
    observerActive: StateFlow<Boolean>,
    key: K,
    onError: (LoadingError, Loadable<T>) -> Unit,
    keepPreviousData: Boolean = false
): StateFlow<Loadable<T>> {
    return observe(
        observerCoroutineScope,
        observerActive,
        MutableStateFlow(key),
        onError,
        keepPreviousData
    )
}

suspend fun <K : Any, T : Any> KeyedPhysicalReplica<T, K>.clearAll() {  // TODO: move to KeyedReplica interface
    onEachReplica {
        clear()
    }
}

suspend fun <K : Any, T : Any> KeyedPhysicalReplica<T, K>.invalidateAll() {
    onEachReplica {
        invalidate()
    }
}