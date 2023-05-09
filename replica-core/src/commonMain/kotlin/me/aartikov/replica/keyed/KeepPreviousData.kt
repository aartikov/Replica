package me.aartikov.replica.keyed

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

    private fun launchStateObserving() {
        var previousData: T? = null
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