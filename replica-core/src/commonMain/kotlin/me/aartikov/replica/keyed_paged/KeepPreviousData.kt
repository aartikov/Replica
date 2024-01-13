package me.aartikov.replica.keyed_paged

import kotlinx.coroutines.CoroutineScope
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
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.Paged
import me.aartikov.replica.paged.PagedData
import me.aartikov.replica.paged.PagedReplicaObserver

/**
 * Modifies [KeyedPagedReplica] so its observer keeps a data from a previous key until a data for a
 * new key will not be loaded.
 * It allows to dramatically improve UX when [KeyedPagedReplica] is observed by changing keys.
 */
fun <K : Any, T : Any, P : Page<T>> KeyedPagedReplica<K, T, P>.keepPreviousData():
            KeyedPagedReplica<K, T, P> {
    return KeepPreviousDataKeyedPagedReplica(this)
}

private class KeepPreviousDataKeyedPagedReplica<K : Any, T : Any, P : Page<T>>(
    private val originalKeyedPagedReplica: KeyedPagedReplica<K, T, P>
) : KeyedPagedReplica<K, T, P> {

    override fun observe(
        observerCoroutineScope: CoroutineScope,
        observerActive: StateFlow<Boolean>,
        key: StateFlow<K?>
    ): PagedReplicaObserver<T, P> {
        val originalObserver = originalKeyedPagedReplica.observe(
            observerCoroutineScope,
            observerActive,
            key
        )

        return KeepPreviousDataPagedReplicaObserver(
            observerCoroutineScope,
            originalObserver
        )
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

private class KeepPreviousDataPagedReplicaObserver<T : Any, P : Page<T>>(
    val coroutineScope: CoroutineScope,
    val originalObserver: PagedReplicaObserver<T, P>
) : PagedReplicaObserver<T, P> {

    private val _stateFlow = MutableStateFlow(Paged<T, P>())
    override val stateFlow: StateFlow<Paged<T, P>> = _stateFlow.asStateFlow()

    override val loadingErrorFlow: Flow<LoadingError>
        get() = originalObserver.loadingErrorFlow

    private var stateObservingJob: Job? = null

    init {
        if (coroutineScope.isActive) {
            launchStateObserving()
        }
    }

    private fun launchStateObserving() {
        var previousData: PagedData<T, P>? = null
        var previousDataCleanupJob: Job? = null
        var keepingPreviousData = false

        stateObservingJob = originalObserver.stateFlow
            .onEach { newValue ->
                if (newValue.data != null) {
                    previousData = newValue.data
                }

                // "data == null && !loading" means that we should clear previous data.
                // But we can't do it immediately because on switching replica key
                // we gets initial value "Loadable(false, null, null)" for a very short time span.
                if (previousData != null && newValue.data == null && !newValue.loading) {
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