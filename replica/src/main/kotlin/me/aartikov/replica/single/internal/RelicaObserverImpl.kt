package me.aartikov.replica.single.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.aartikov.replica.lifecycle.toActivableFlow
import me.aartikov.replica.single.*
import java.util.*

internal class ReplicaObserverImpl<T : Any>(
    private val coroutineScope: CoroutineScope,
    private val activeFlow: StateFlow<Boolean>,
    private val replicaStateFlow: StateFlow<ReplicaState<T>>,
    private val replicaEventFlow: Flow<ReplicaEvent<T>>,
    private val dispatchAction: (Action.ObserverAction) -> Unit
) : ReplicaObserver<T> {

    private val _stateFlow = MutableStateFlow(Loadable<T>())
    override val stateFlow: StateFlow<Loadable<T>> = _stateFlow.asStateFlow()

    private val _errorEventFlow = MutableSharedFlow<Exception>()
    override val errorEventFlow: Flow<Exception> = _errorEventFlow.asSharedFlow()

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
        launchErrorEventsObserving()
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

    private fun launchErrorEventsObserving() {
        errorEventsObservingJob = coroutineScope.launch {
            replicaEventFlow
                .toActivableFlow(coroutineScope, activeFlow)
                .filterIsInstance<ReplicaEvent.ErrorEvent>()
                .collect { errorEvent ->
                    _errorEventFlow.emit(errorEvent.error)
                }
        }
    }

    private fun launchObserverStatusObserving() {
        observerStatusObservingJob = coroutineScope.launch {
            val observerUuid = UUID.randomUUID().toString()
            try {
                dispatchAction(
                    Action.ObserverAction.ObserverAdded(observerUuid, activeFlow.value)
                )
                activeFlow
                    .collect { active ->
                        if (active) {
                            dispatchAction(Action.ObserverAction.ObserverActive(observerUuid))
                        } else {
                            dispatchAction(Action.ObserverAction.ObserverInactive(observerUuid))
                        }
                    }
            } finally {
                dispatchAction(
                    Action.ObserverAction.ObserverRemoved(observerUuid)
                )
            }
        }
    }
}