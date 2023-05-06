package me.aartikov.replica.keyed

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import me.aartikov.replica.common.LoadingError
import me.aartikov.replica.single.Loadable
import me.aartikov.replica.single.ReplicaObserver

/**
 * Modifies [KeyedReplica] so its observer keeps a data from a previous key until a data for a new key will not be loaded.
 * It allows to dramatically improve UX when [KeyedReplica] is observed by changing keys.
 */
fun <K : Any, T : Any> KeyedReplica<K, T>.keepPreviousData(): KeyedReplica<K, T> {
    return KeepPreviousDataKeyedReplica(this)
}

private class KeepPreviousDataKeyedReplica<K : Any, T : Any>(
    private val originalKeyedReplica: KeyedReplica<K, T>
) : KeyedReplica<K, T> {

    override fun observe(
        observerCoroutineScope: CoroutineScope,
        observerActive: StateFlow<Boolean>,
        key: StateFlow<K?>
    ): ReplicaObserver<T> {
        val originalObserver = originalKeyedReplica.observe(
            observerCoroutineScope,
            observerActive,
            key
        )

        return KeepPreviousDataReplicaObserver(
            observerCoroutineScope,
            originalObserver
        )
    }

    override fun refresh(key: K) {
        originalKeyedReplica.refresh(key)
    }

    override fun revalidate(key: K) {
        originalKeyedReplica.revalidate(key)
    }

    override suspend fun getData(key: K, forceRefresh: Boolean): T {
        return originalKeyedReplica.getData(key, forceRefresh)
    }
}

private class KeepPreviousDataReplicaObserver<T : Any>(
    val coroutineScope: CoroutineScope,
    val originalObserver: ReplicaObserver<T>
) : ReplicaObserver<T> {

    private val _stateFlow = MutableStateFlow(Loadable<T>())
    override val stateFlow: StateFlow<Loadable<T>> = _stateFlow.asStateFlow()

    override val loadingErrorFlow: Flow<LoadingError>
        get() = originalObserver.loadingErrorFlow

    private var stateObservingJob: Job? = null

    init {
        if (coroutineScope.isActive) {
            launchStateObserving()
        }
    }

    @OptIn(FlowPreview::class)
    private fun launchStateObserving() {
        stateObservingJob = originalObserver.stateFlow
            .debounce { newValue ->
                if (newValue.data == null && !newValue.loading) {
                    30 // wait until an empty replica starts load data
                } else {
                    0
                }
            }
            .onEach { newValue ->
                if (newValue.data == null && newValue.loading) {
                    _stateFlow.value = newValue.copy(data = _stateFlow.value.data)
                } else {
                    _stateFlow.value = newValue
                }
            }
            .launchIn(coroutineScope)
    }

    override fun cancelObserving() {
        originalObserver.cancelObserving()
        stateObservingJob?.cancel()
    }
}