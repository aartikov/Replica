package me.aartikov.replica.algebra.normal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import me.aartikov.replica.common.LoadingError
import me.aartikov.replica.keyed.KeyedReplica
import me.aartikov.replica.single.Loadable
import me.aartikov.replica.single.Replica
import me.aartikov.replica.single.ReplicaObserver

/**
 * Creates [KeyedReplica] by providing [Replica] for each given key.
 */
fun <K : Any, T : Any> associate(replicaProvider: (K) -> Replica<T>): KeyedReplica<K, T> {
    return AssociatedKeyedReplica(replicaProvider)
}

private class AssociatedKeyedReplica<K : Any, T : Any>(
    private val replicaProvider: (K) -> Replica<T>
) : KeyedReplica<K, T> {

    override fun observe(
        observerCoroutineScope: CoroutineScope,
        observerActive: StateFlow<Boolean>,
        key: StateFlow<K?>
    ): ReplicaObserver<T> {
        return AssociatedReplicaObserver(
            observerCoroutineScope,
            observerActive,
            key,
            replicaProvider
        )
    }

    override fun refresh(key: K) {
        replicaProvider(key).refresh()
    }

    override fun revalidate(key: K) {
        replicaProvider(key).revalidate()
    }

    override suspend fun getData(key: K, forceRefresh: Boolean): T {
        return replicaProvider(key).getData(forceRefresh)
    }
}

private class AssociatedReplicaObserver<T : Any, K : Any>(
    private val coroutineScope: CoroutineScope,
    private val activeFlow: StateFlow<Boolean>,
    private val key: StateFlow<K?>,
    private val replicaProvider: (K) -> Replica<T>
) : ReplicaObserver<T> {

    private val _stateFlow = MutableStateFlow(Loadable<T>())
    override val stateFlow: StateFlow<Loadable<T>> = _stateFlow.asStateFlow()

    private val _loadingErrorFlow = MutableSharedFlow<LoadingError>(extraBufferCapacity = 1000)
    override val loadingErrorFlow: Flow<LoadingError> = _loadingErrorFlow.asSharedFlow()

    private var currentReplica: Replica<T>? = null
    private var currentReplicaObserver: ReplicaObserver<T>? = null
    private var stateObservingJob: Job? = null
    private var errorsObservingJob: Job? = null

    init {
        if (coroutineScope.isActive) {
            launchObserving()
        }
    }

    override fun cancelObserving() {
        cancelCurrentObserving()
    }

    private fun launchObserving() {
        key
            .onEach { currentKey ->
                cancelCurrentObserving()
                launchObservingForKey(currentKey)
            }
            .launchIn(coroutineScope)
    }

    private fun launchObservingForKey(currentKey: K?) {
        currentReplica = currentKey?.let { replicaProvider(currentKey) }
        currentReplicaObserver = currentReplica?.observe(coroutineScope, activeFlow)

        val currentReplicaObserver = currentReplicaObserver
        if (currentReplicaObserver == null) {
            _stateFlow.value = Loadable()
            return
        }

        stateObservingJob = currentReplicaObserver.stateFlow
            .onEach {
                _stateFlow.value = it
            }
            .launchIn(coroutineScope)

        errorsObservingJob = currentReplicaObserver.loadingErrorFlow
            .onEach {
                _loadingErrorFlow.emit(it)
            }
            .launchIn(coroutineScope)
    }

    private fun cancelCurrentObserving() {
        currentReplica = null
        currentReplicaObserver?.cancelObserving()
        currentReplicaObserver = null

        stateObservingJob?.cancel()
        stateObservingJob = null

        errorsObservingJob?.cancel()
        errorsObservingJob = null
    }
}