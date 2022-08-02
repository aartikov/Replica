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

/**
 * Creates a replica from [Flow] of data.
 */
fun <T : Any> flowReplica(flow: Flow<T>): Replica<T> {
    return FlowReplica(flow)
}

private class FlowReplica<T : Any>(
    private val flow: Flow<T>
) : Replica<T> {

    override fun observe(
        observerCoroutineScope: CoroutineScope,
        observerActive: StateFlow<Boolean>
    ): ReplicaObserver<T> {
        return FlowReplicaObserver(observerCoroutineScope, observerActive, flow)
    }

    override fun refresh() {
        // nothing
    }

    override fun revalidate() {
        // nothing
    }

    override suspend fun getData(forceRefresh: Boolean): T {
        return flow.first()
    }
}

private class FlowReplicaObserver<T : Any>(
    private val coroutineScope: CoroutineScope,
    private val activeFlow: StateFlow<Boolean>,
    private val dataFlow: Flow<T>
) : ReplicaObserver<T> {

    private val _stateFlow = MutableStateFlow(Loadable<T>())
    override val stateFlow: StateFlow<Loadable<T>> = _stateFlow.asStateFlow()

    override val loadingErrorFlow: Flow<LoadingError> = MutableSharedFlow() // emits nothing

    private var dataObservingJob: Job? = null

    init {
        if (coroutineScope.isActive) {
            launchStateObserving()
        }
    }

    private fun launchStateObserving() {
        dataObservingJob = dataFlow
            .toActivableFlow(coroutineScope, activeFlow)
            .onEach { data ->
                _stateFlow.value = Loadable(data = data)
            }
            .launchIn(coroutineScope)
    }

    override fun cancelObserving() {
        dataObservingJob?.cancel()
        dataObservingJob = null
    }
}