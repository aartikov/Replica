package me.aartikov.replica.keyed_paged

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.aartikov.replica.common.LoadingError
import me.aartikov.replica.common.ReplicaObserverHost
import me.aartikov.replica.paged.Paged
import me.aartikov.replica.paged.PagedLoadingStatus
import me.aartikov.replica.paged.PagedReplicaObserver

/**
 * Modifies [KeyedPagedReplica] so its observer keeps a data from a previous key until a data for a
 * new key will not be loaded.
 * It allows to dramatically improve UX when [KeyedPagedReplica] is observed by changing keys.
 */
fun <K : Any, T : Any> KeyedPagedReplica<K, T>.keepPreviousData():
            KeyedPagedReplica<K, T> {
    return KeepPreviousDataKeyedPagedReplica(this)
}

private class KeepPreviousDataKeyedPagedReplica<K : Any, T : Any>(
    private val originalKeyedPagedReplica: KeyedPagedReplica<K, T>
) : KeyedPagedReplica<K, T> {

    override fun observe(
        observerHost: ReplicaObserverHost,
        keyFlow: StateFlow<K?>
    ): PagedReplicaObserver<T> {
        val originalObserver = originalKeyedPagedReplica.observe(observerHost, keyFlow)
        return KeepPreviousDataPagedReplicaObserver(observerHost, originalObserver)
    }

    override fun refresh(key: K) {
        originalKeyedPagedReplica.refresh(key)
    }

    override fun revalidate(key: K) {
        originalKeyedPagedReplica.revalidate(key)
    }

    override fun loadNext(key: K) {
        originalKeyedPagedReplica.loadNext(key)
    }

    override fun loadPrevious(key: K) {
        originalKeyedPagedReplica.loadPrevious(key)
    }
}

private class KeepPreviousDataPagedReplicaObserver<T : Any>(
    observerHost: ReplicaObserverHost,
    val originalObserver: PagedReplicaObserver<T>
) : PagedReplicaObserver<T> {

    private val coroutineScope = observerHost.observerCoroutineScope

    private val _stateFlow = MutableStateFlow(Paged<T>())
    override val stateFlow: StateFlow<Paged<T>> = _stateFlow.asStateFlow()

    override val loadingErrorFlow: Flow<LoadingError>
        get() = originalObserver.loadingErrorFlow

    private var stateObservingJob: Job? = null

    init {
        if (coroutineScope.isActive) {
            launchStateObserving()
        }
    }

    private fun launchStateObserving() {
        var previousData: T? = null
        var previousDataCleanupJob: Job? = null
        var keepingPreviousData = false

        stateObservingJob = originalObserver.stateFlow
            .onEach { newValue ->
                if (newValue.data != null) {
                    previousData = newValue.data
                }

                // "data == null and PagedLoadingStatus.None" means that we should clear previous data.
                // But we can't do it immediately because on switching replica key
                // we gets initial value "Paged(PagedLoadingStatus.None, null, null)" for a very short time span.
                if (previousData != null && newValue.data == null && newValue.loadingStatus == PagedLoadingStatus.None) {
                    previousDataCleanupJob?.cancel()
                    previousDataCleanupJob = coroutineScope.launch {
                        delay(30)
                        previousData = null
                        previousDataCleanupJob = null
                        if (keepingPreviousData) {
                            _stateFlow.value = _stateFlow.value.copy(data = null)
                            keepingPreviousData = false
                        }
                    }
                } else if (previousDataCleanupJob != null) {
                    previousDataCleanupJob?.cancel()
                    previousDataCleanupJob = null
                }

                _stateFlow.value =
                    if (previousData != null && newValue.data == null && newValue.error == null) {
                        keepingPreviousData = true
                        newValue.copy(data = previousData)
                    } else {
                        keepingPreviousData = false
                        newValue
                    }
            }
            .launchIn(coroutineScope)
    }

    override fun cancelObserving() {
        originalObserver.cancelObserving()
        stateObservingJob?.cancel()
    }
}