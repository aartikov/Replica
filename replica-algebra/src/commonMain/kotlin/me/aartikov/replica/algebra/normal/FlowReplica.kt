package me.aartikov.replica.algebra.normal

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import me.aartikov.replica.algebra.internal.toActivableFlow
import me.aartikov.replica.common.LoadingError
import me.aartikov.replica.common.ReplicaObserverHost
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

    override fun observe(observerHost: ReplicaObserverHost): ReplicaObserver<T> {
        return FlowReplicaObserver(observerHost, flow)
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
    private val observerHost: ReplicaObserverHost,
    private val dataFlow: Flow<T>
) : ReplicaObserver<T> {

    private val coroutineScope = observerHost.observerCoroutineScope
    private val activeFlow = observerHost.observerActive

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