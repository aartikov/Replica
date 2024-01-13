package me.aartikov.replica.keyed_paged.internal

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
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.Paged
import me.aartikov.replica.paged.PagedReplica
import me.aartikov.replica.paged.PagedReplicaObserver

internal class KeyedPagedReplicaObserverImpl<T : Any, K : Any, P : Page<T>>(
    private val coroutineScope: CoroutineScope,
    private val activeFlow: StateFlow<Boolean>,
    private val key: StateFlow<K?>,
    private val replicaProvider: (K) -> PagedReplica<T, P>
) : PagedReplicaObserver<T, P> {

    private val _stateFlow = MutableStateFlow(Paged<T, P>())
    override val stateFlow: StateFlow<Paged<T, P>> = _stateFlow.asStateFlow()

    private val _loadingErrorFlow = MutableSharedFlow<LoadingError>(extraBufferCapacity = 1000)
    override val loadingErrorFlow: Flow<LoadingError> = _loadingErrorFlow.asSharedFlow()

    private var currentReplica: PagedReplica<T, P>? = null
    private var currentReplicaObserver: PagedReplicaObserver<T, P>? = null
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