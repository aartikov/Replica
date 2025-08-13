package me.aartikov.replica.algebra.paged

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import me.aartikov.replica.algebra.normal.withKey
import me.aartikov.replica.common.LoadingError
import me.aartikov.replica.common.ReplicaObserverHost
import me.aartikov.replica.keyed.KeyedReplica
import me.aartikov.replica.keyed_paged.KeyedPagedReplica
import me.aartikov.replica.paged.Paged
import me.aartikov.replica.paged.PagedLoadingStatus
import me.aartikov.replica.paged.PagedReplica
import me.aartikov.replica.paged.PagedReplicaObserver
import me.aartikov.replica.single.Replica
import me.aartikov.replica.single.ReplicaObserver

/**
 * Transforms [Replica] to [PagedReplica].
 */
fun <T : Any> Replica<T>.toPaged(): PagedReplica<T> {
    return NormalToPagedReplica(this)
}

/**
 * Transforms [KeyedReplica] to [KeyedPagedReplica].
 */
fun <K : Any, T : Any> KeyedReplica<K, T>.toPaged(): KeyedPagedReplica<K, T> {
    return associatePaged { key ->
        withKey(key).toPaged()
    }
}

private class NormalToPagedReplica<T : Any>(
    private val originalReplica: Replica<T>
) : PagedReplica<T> {

    override fun observe(observerHost: ReplicaObserverHost): PagedReplicaObserver<T> {
        val originalObserver = originalReplica.observe(observerHost)

        return NormalToPagedReplicaObserver(
            observerHost.observerCoroutineScope,
            originalObserver
        )
    }

    override fun refresh() {
        originalReplica.refresh()
    }

    override fun revalidate() {
        originalReplica.revalidate()
    }

    override fun loadNext() {
        // nothing
    }

    override fun loadPrevious() {
        // nothing
    }
}

private class NormalToPagedReplicaObserver<T : Any>(
    private val coroutineScope: CoroutineScope,
    private val originalObserver: ReplicaObserver<T>
) : PagedReplicaObserver<T> {

    private val _stateFlow = MutableStateFlow(Paged<T>())
    override val stateFlow: StateFlow<Paged<T>> = _stateFlow.asStateFlow()

    override val loadingErrorFlow: Flow<LoadingError> = originalObserver.loadingErrorFlow

    private var stateObservingJob: Job? = null

    init {
        if (coroutineScope.isActive) {
            launchStateObserving()
        }
    }

    private fun launchStateObserving() {
        stateObservingJob = originalObserver.stateFlow
            .onEach { state ->
                _stateFlow.value = Paged(
                    loadingStatus = if (state.loading) PagedLoadingStatus.LoadingFirstPage else PagedLoadingStatus.None,
                    data = state.data,
                    error = state.error
                )
            }
            .launchIn(coroutineScope)
    }

    override fun cancelObserving() {
        originalObserver.cancelObserving()
        stateObservingJob?.cancel()
        stateObservingJob = null
    }
}
