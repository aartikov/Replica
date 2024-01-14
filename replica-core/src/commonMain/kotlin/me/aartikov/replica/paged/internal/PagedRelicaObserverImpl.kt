package me.aartikov.replica.paged.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.aartikov.replica.common.LoadingError
import me.aartikov.replica.common.internal.AtomicLong
import me.aartikov.replica.common.internal.toActivableFlow
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.Paged
import me.aartikov.replica.paged.PagedData
import me.aartikov.replica.paged.PagedReplicaEvent
import me.aartikov.replica.paged.PagedReplicaObserver
import me.aartikov.replica.paged.PagedReplicaState
import me.aartikov.replica.paged.internal.controllers.ObserversController
import me.aartikov.replica.paged.toPaged

internal class PagedReplicaObserverImpl<I : Any, P : Page<I>>(
    private val coroutineScope: CoroutineScope,
    private val activeFlow: StateFlow<Boolean>,
    private val replicaStateFlow: StateFlow<PagedReplicaState<I, P>>,
    private val replicaEventFlow: Flow<PagedReplicaEvent<I, P>>,
    private val observersController: ObserversController<I, P>
) : PagedReplicaObserver<PagedData<I, P>> {

    companion object {
        private val idGenerator = AtomicLong(0)
    }

    private val _stateFlow = MutableStateFlow(Paged<PagedData<I, P>>())
    override val stateFlow: StateFlow<Paged<PagedData<I, P>>> = _stateFlow.asStateFlow()

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
                _stateFlow.value = replicaState.toPaged()
            }
            .launchIn(coroutineScope)
    }

    private fun launchLoadingErrorsObserving() {
        errorsObservingJob = replicaEventFlow
            .toActivableFlow(coroutineScope, activeFlow)
            .filterIsInstance<PagedReplicaEvent.LoadingEvent.LoadingFinished.Error>()
            .onEach { errorEvent ->
                _loadingErrorFlow.emit(LoadingError(errorEvent.reason, errorEvent.exception))
            }
            .launchIn(coroutineScope)
    }
}