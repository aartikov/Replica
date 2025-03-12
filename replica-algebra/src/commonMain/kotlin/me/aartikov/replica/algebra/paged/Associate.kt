package me.aartikov.replica.algebra.paged

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
import me.aartikov.replica.common.ReplicaObserverHost
import me.aartikov.replica.keyed_paged.KeyedPagedReplica
import me.aartikov.replica.paged.Paged
import me.aartikov.replica.paged.PagedReplica
import me.aartikov.replica.paged.PagedReplicaObserver

/**
 * Creates [KeyedPagedReplica] by providing [PagedReplica] for each given key.
 */
fun <K : Any, T : Any> associatePaged(replicaProvider: (K) -> PagedReplica<T>): KeyedPagedReplica<K, T> {
    return AssociatedKeyedReplica(replicaProvider)
}

private class AssociatedKeyedReplica<K : Any, T : Any>(
    private val replicaProvider: (K) -> PagedReplica<T>
) : KeyedPagedReplica<K, T> {

    override fun observe(
        observerHost: ReplicaObserverHost,
        keyFlow: StateFlow<K?>
    ): PagedReplicaObserver<T> {
        return AssociatedReplicaObserver(observerHost, keyFlow, replicaProvider)
    }

    override fun refresh(key: K) {
        replicaProvider(key).refresh()
    }

    override fun revalidate(key: K) {
        replicaProvider(key).revalidate()
    }

    override fun loadNext(key: K) {
        replicaProvider(key).loadNext()
    }

    override fun loadPrevious(key: K) {
        replicaProvider(key).loadPrevious()
    }
}

private class AssociatedReplicaObserver<T : Any, K : Any>(
    private val observerHost: ReplicaObserverHost,
    private val keyFlow: StateFlow<K?>,
    private val replicaProvider: (K) -> PagedReplica<T>
) : PagedReplicaObserver<T> {

    private val coroutineScope = observerHost.observerCoroutineScope

    private val _stateFlow = MutableStateFlow(Paged<T>())
    override val stateFlow: StateFlow<Paged<T>> = _stateFlow.asStateFlow()

    private val _loadingErrorFlow = MutableSharedFlow<LoadingError>(extraBufferCapacity = 1000)
    override val loadingErrorFlow: Flow<LoadingError> = _loadingErrorFlow.asSharedFlow()

    private var currentReplica: PagedReplica<T>? = null
    private var currentReplicaObserver: PagedReplicaObserver<T>? = null
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
        keyFlow
            .onEach { currentKey ->
                cancelCurrentObserving()
                launchObservingForKey(currentKey)
            }
            .launchIn(coroutineScope)
    }

    private fun launchObservingForKey(currentKey: K?) {
        currentReplica = currentKey?.let { replicaProvider(currentKey) }
        currentReplicaObserver = currentReplica?.observe(observerHost)

        val currentReplicaObserver = currentReplicaObserver
        if (currentReplicaObserver == null) {
            _stateFlow.value = Paged()
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