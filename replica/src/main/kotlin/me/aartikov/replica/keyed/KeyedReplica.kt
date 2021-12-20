package me.aartikov.replica.keyed

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.single.Loadable
import me.aartikov.replica.single.ReplicaObserver
import me.aartikov.replica.single.invalidate

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
    onError: (Exception, Loadable<T>) -> Unit,
    keepPreviousData: Boolean = false
): StateFlow<Loadable<T>> {
    val observer = observe(observerCoroutineScope, observerActive, key)
    observer
        .errorEventFlow
        .onEach { exception ->
            onError(exception, observer.stateFlow.value)
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
    onError: (Exception, Loadable<T>) -> Unit,
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

fun <K : Any, T : Any> KeyedPhysicalReplica<T, K>.clearAll() =
    onEachReplica { clear() } // TODO: move to KeyedReplica interface

fun <K : Any, T : Any> KeyedPhysicalReplica<T, K>.invalidateAll() = onEachReplica { invalidate() }