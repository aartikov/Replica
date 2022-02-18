package me.aartikov.replica.algebra

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import me.aartikov.replica.algebra.internal.toActivableFlow
import me.aartikov.replica.common.LoadingError
import me.aartikov.replica.single.Loadable
import me.aartikov.replica.single.Replica
import me.aartikov.replica.single.ReplicaObserver

fun <T : Any> stateFlowReplica(stateFlow: StateFlow<T>): Replica<T> {
    return StateFlowReplica(stateFlow)
}

private class StateFlowReplica<T : Any>(
    private val stateFlow: StateFlow<T>
) : Replica<T> {

    override fun observe(
        observerCoroutineScope: CoroutineScope,
        observerActive: StateFlow<Boolean>
    ): ReplicaObserver<T> {
        return StateFlowReplicaObserver(observerCoroutineScope, observerActive, stateFlow)
    }

    override fun refresh() {
        // nothing
    }

    override fun revalidate() {
        // nothing
    }

    override suspend fun getData(): T {
        return stateFlow.value
    }

    override suspend fun getRefreshedData(): T {
        return stateFlow.value
    }
}

private class StateFlowReplicaObserver<T : Any>(
    private val coroutineScope: CoroutineScope,
    private val activeFlow: StateFlow<Boolean>,
    private val originalStateFlow: StateFlow<T>
) : ReplicaObserver<T> {

    private val _stateFlow = MutableStateFlow(Loadable(data = originalStateFlow.value))
    override val stateFlow: StateFlow<Loadable<T>> = _stateFlow.asStateFlow()

    override val loadingErrorFlow: Flow<LoadingError> = MutableSharedFlow() // emits nothing

    private var stateObservingJob: Job? = null

    init {
        if (coroutineScope.isActive) {
            launchStateObserving()
        }
    }

    private fun launchStateObserving() {
        stateObservingJob = originalStateFlow
            .toActivableFlow(coroutineScope, activeFlow)
            .onEach { data ->
                _stateFlow.value = Loadable(data = data)
            }
            .launchIn(coroutineScope)
    }

    override fun cancelObserving() {
        stateObservingJob?.cancel()
        stateObservingJob = null
    }
}