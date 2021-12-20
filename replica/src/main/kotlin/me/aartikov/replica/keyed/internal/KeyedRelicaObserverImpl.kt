package me.aartikov.replica.keyed.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.aartikov.replica.single.Loadable
import me.aartikov.replica.single.Replica
import me.aartikov.replica.single.ReplicaObserver

internal class KeyedReplicaObserverImpl<T : Any, K : Any>(
    private val coroutineScope: CoroutineScope,
    private val activeFlow: StateFlow<Boolean>,
    private val key: StateFlow<K?>,
    private val replicaProvider: (K) -> Replica<T>
) : ReplicaObserver<T> {

    private val _stateFlow = MutableStateFlow(Loadable<T>())
    override val stateFlow: StateFlow<Loadable<T>> = _stateFlow.asStateFlow()

    private val _errorEventFlow = MutableSharedFlow<Exception>()
    override val errorEventFlow: Flow<Exception> = _errorEventFlow.asSharedFlow()

    private var currentReplica: Replica<T>? = null
    private var currentReplicaObserver: ReplicaObserver<T>? = null
    private var stateObservingJob: Job? = null
    private var errorEventsObservingJob: Job? = null

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

        stateObservingJob = coroutineScope.launch {
            currentReplicaObserver.stateFlow
                .collect {
                    _stateFlow.value = it
                }
        }

        errorEventsObservingJob = coroutineScope.launch {
            currentReplicaObserver.errorEventFlow
                .collect {
                    _errorEventFlow.emit(it)
                }
        }
    }

    private fun cancelCurrentObserving() {
        currentReplica = null
        currentReplicaObserver?.cancelObserving()
        currentReplicaObserver = null

        stateObservingJob?.cancel()
        stateObservingJob = null

        errorEventsObservingJob?.cancel()
        errorEventsObservingJob = null
    }
}