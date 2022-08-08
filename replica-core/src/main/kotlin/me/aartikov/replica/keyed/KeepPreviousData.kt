package me.aartikov.replica.keyed

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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

        stateObservingJob = originalObserver.stateFlow
            .onEach { newValue ->
                if (newValue.data != null) {
                    previousData = newValue.data
                }

                // "data == null && !loading" means that we should clear previous data.
                // But we can't do it immediately because on switching replica key
                // we gets initial value "Loadable(false, null, null)" for a very short time span.
                if (newValue.data == null && !newValue.loading) {
                    previousDataCleanupJob?.cancel()
                    previousDataCleanupJob = coroutineScope.launch {
                        delay(100)
                        previousData = null
                        previousDataCleanupJob = null
                    }
                } else if (previousDataCleanupJob != null) {
                    previousDataCleanupJob?.cancel()
                    previousDataCleanupJob = null
                }

                _stateFlow.value = if (newValue.data == null && newValue.loading) {
                    newValue.copy(data = previousData)
                } else {
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