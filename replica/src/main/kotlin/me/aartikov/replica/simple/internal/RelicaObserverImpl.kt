package me.aartikov.replica.simple.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.aartikov.replica.lifecycle.toActivableFlow
import me.aartikov.replica.simple.*
import java.util.*

internal class ReplicaObserverImpl<T : Any>(
    private val coroutineScope: CoroutineScope,
    private val activeFlow: StateFlow<Boolean>,
    private val replicaStateFlow: StateFlow<ReplicaState<T>>,
    private val replicaEventFlow: Flow<ReplicaEvent<T>>,
    private val dispatchAction: (Action.ObserverAction) -> Unit
) : ReplicaObserver<T> {

    private val _stateFlow = MutableStateFlow(replicaStateFlow.value.toLoadable())
    override val stateFlow: StateFlow<Loadable<T>> = _stateFlow.asStateFlow()

    private val _errorEventFlow = MutableSharedFlow<Exception>()
    override val errorEventFlow: Flow<Exception> = _errorEventFlow.asSharedFlow()

    init {
        if (coroutineScope.isActive) {
            launchObserving()
        }
    }

    private fun launchObserving() {
        launchStateObserving()
        launchErrorEventsObserving()
        launchObserverStatusObserving()
    }

    private fun launchStateObserving() {
        replicaStateFlow
            .toActivableFlow(coroutineScope, activeFlow)
            .onEach { replicaState ->
                _stateFlow.value = replicaState.toLoadable()
            }
            .launchIn(coroutineScope)
    }

    private fun launchErrorEventsObserving() {
        replicaEventFlow
            .toActivableFlow(coroutineScope, activeFlow)
            .filterIsInstance<ReplicaEvent.ErrorEvent>()
            .onEach { errorEvent ->
                _errorEventFlow.emit(errorEvent.error)
            }
            .launchIn(coroutineScope)
    }

    private fun launchObserverStatusObserving() {
        coroutineScope.launch {
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