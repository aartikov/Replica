package me.aartikov.replica.single.internal

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import me.aartikov.replica.single.*
import me.aartikov.replica.single.internal.controllers.ObserversController
import me.aartikov.replica.utils.toActivableFlow
import java.util.*

internal class ReplicaObserverImpl<T : Any>(
    private val coroutineScope: CoroutineScope,
    private val activeFlow: StateFlow<Boolean>,
    private val replicaStateFlow: StateFlow<ReplicaState<T>>,
    private val replicaEventFlow: Flow<ReplicaEvent<T>>,
    private val observersController: ObserversController<T>
) : ReplicaObserver<T> {

    private val _stateFlow = MutableStateFlow(Loadable<T>())
    override val stateFlow: StateFlow<Loadable<T>> = _stateFlow.asStateFlow()

    private val _loadingErrorFlow = MutableSharedFlow<LoadingError>(extraBufferCapacity = 1000)
    override val loadingErrorFlow: Flow<LoadingError> = _loadingErrorFlow.asSharedFlow()

    private var stateObservingJob: Job? = null
    private var errorEventsObservingJob: Job? = null
    private var observerStatusObservingJob: Job? = null

    init {
        if (coroutineScope.isActive) {
            launchObserving()
        }
    }

    override fun cancelObserving() {
        stateObservingJob?.cancel()
        stateObservingJob = null

        errorEventsObservingJob?.cancel()
        errorEventsObservingJob = null

        observerStatusObservingJob?.cancel()
        observerStatusObservingJob = null
    }

    private fun launchObserving() {
        launchStateObserving()
        launchLoadingErrorsObserving()
        launchObserverStatusObserving()
    }

    private fun launchStateObserving() {
        stateObservingJob = coroutineScope.launch {
            replicaStateFlow
                .toActivableFlow(coroutineScope, activeFlow)
                .collect { replicaState ->
                    _stateFlow.value = replicaState.toLoadable()
                }
        }
    }

    private fun launchLoadingErrorsObserving() {
        errorEventsObservingJob = coroutineScope.launch {
            replicaEventFlow
                .toActivableFlow(coroutineScope, activeFlow)
                .filterIsInstance<ReplicaEvent.LoadingEvent.LoadingFinished.Error>()
                .collect { errorEvent ->
                    _loadingErrorFlow.emit(LoadingError(errorEvent.exception))
                }
        }
    }

    private fun launchObserverStatusObserving() {
        observerStatusObservingJob = coroutineScope.launch {
            val observerUuid = UUID.randomUUID().toString()
            try {
                observersController.onObserverAdded(observerUuid, activeFlow.value)
                activeFlow
                    .collect { active ->
                        if (active) {
                            observersController.onObserverActive(observerUuid)
                        } else {
                            observersController.onObserverInactive(observerUuid)
                        }
                    }
            } finally {
                withContext(NonCancellable) {
                    observersController.onObserverRemoved(observerUuid)
                }
            }
        }
    }
}