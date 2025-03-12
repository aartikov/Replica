package me.aartikov.replica.algebra.paged

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import me.aartikov.replica.algebra.internal.toActivableFlow
import me.aartikov.replica.common.LoadingError
import me.aartikov.replica.common.ReplicaObserverHost
import me.aartikov.replica.paged.Paged
import me.aartikov.replica.paged.PagedReplica
import me.aartikov.replica.paged.PagedReplicaObserver

/**
 * Creates a paged replica from [Flow] of data.
 */
fun <T : Any> flowPagedReplica(flow: Flow<T>): PagedReplica<T> {
    return FlowReplica(flow)
}

private class FlowReplica<T : Any>(private val flow: Flow<T>) : PagedReplica<T> {

    override fun observe(observerHost: ReplicaObserverHost): PagedReplicaObserver<T> {
        return FlowReplicaObserver(observerHost, flow)
    }

    override fun refresh() {
        // nothing
    }

    override fun revalidate() {
        // nothing
    }

    override fun loadNext() {
        // nothing
    }

    override fun loadPrevious() {
        // nothing
    }
}

private class FlowReplicaObserver<T : Any>(
    observerHost: ReplicaObserverHost,
    private val dataFlow: Flow<T>
) : PagedReplicaObserver<T> {

    private val coroutineScope = observerHost.observerCoroutineScope
    private val activeFlow = observerHost.observerActive

    private val _stateFlow = MutableStateFlow(Paged<T>())
    override val stateFlow: StateFlow<Paged<T>> = _stateFlow.asStateFlow()

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
                _stateFlow.value = Paged(data = data)
            }
            .launchIn(coroutineScope)
    }

    override fun cancelObserving() {
        dataObservingJob?.cancel()
        dataObservingJob = null
    }
}