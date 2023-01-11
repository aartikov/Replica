package me.aartikov.replica.single.internal

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import me.aartikov.replica.common.LoadingError
import me.aartikov.replica.common.internal.AtomicLong
import me.aartikov.replica.common.internal.toActivableFlow
import me.aartikov.replica.single.*
import me.aartikov.replica.single.internal.controllers.ObserversController

internal class ReplicaObserverImpl<T : Any>(
    private val coroutineScope: CoroutineScope,
    private val activeFlow: StateFlow<Boolean>,
    private val replicaStateFlow: StateFlow<ReplicaState<T>>,
    private val replicaEventFlow: Flow<ReplicaEvent<T>>,
    private val observersController: ObserversController<T>
) : ReplicaObserver<T> {

    companion object {
        private val idGenerator = AtomicLong(0)
    }

    private val _stateFlow = MutableStateFlow(Loadable<T>())
    override val stateFlow: StateFlow<Loadable<T>> = _stateFlow.asStateFlow()

    private val _loadingErrorFlow = MutableSharedFlow<LoadingError>(extraBufferCapacity = 1000)
    override val loadingErrorFlow: Flow<LoadingError> = _loadingErrorFlow.asSharedFlow()

    private var observerControllingJob: Job? = null
    private var stateObservingJob: Job? = null
    private var errorsObservingJob: Job? = null

    init {
        if (coroutineScope.isActive) {
            launchObserverControlling()
            launchStateObserving()
            launchLoadingErrorsObserving()
        }
    }

    override fun cancelObserving() {
        observerControllingJob?.cancel()
        observerControllingJob = null

        stateObservingJob?.cancel()
        stateObservingJob = null

        errorsObservingJob?.cancel()
        errorsObservingJob = null
    }

    private fun launchObserverControlling() {
        observerControllingJob = coroutineScope.launch {
            val observerId = idGenerator.addAndGet(1)
            try {
                observersController.onObserverAdded(observerId, activeFlow.value)
                activeFlow
                    .collect { active ->
                        if (active) {
                            observersController.onObserverActive(observerId)
                        } else {
                            observersController.onObserverInactive(observerId)
                        }
                    }
            } finally {
                withContext(NonCancellable) {
                    observersController.onObserverRemoved(observerId)
                }
            }
        }
    }

    private fun launchStateObserving() {
        stateObservingJob = replicaStateFlow
            .toActivableFlow(coroutineScope, activeFlow)
            .onEach { replicaState ->
                _stateFlow.value = replicaState.toLoadable()
            }
            .launchIn(coroutineScope)
    }

    private fun launchLoadingErrorsObserving() {
        errorsObservingJob = replicaEventFlow
            .toActivableFlow(coroutineScope, activeFlow)
            .filterIsInstance<ReplicaEvent.LoadingEvent.LoadingFinished.Error>()
            .onEach { errorEvent ->
                _loadingErrorFlow.emit(LoadingError(errorEvent.exception))
            }
            .launchIn(coroutineScope)
    }
}