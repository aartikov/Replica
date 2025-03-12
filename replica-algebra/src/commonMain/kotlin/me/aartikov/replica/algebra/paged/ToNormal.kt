package me.aartikov.replica.algebra.paged

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import me.aartikov.replica.algebra.normal.associate
import me.aartikov.replica.common.LoadingError
import me.aartikov.replica.common.ReplicaObserverHost
import me.aartikov.replica.keyed.KeyedReplica
import me.aartikov.replica.keyed_paged.KeyedPagedReplica
import me.aartikov.replica.paged.PagedReplica
import me.aartikov.replica.paged.PagedReplicaObserver
import me.aartikov.replica.single.Loadable
import me.aartikov.replica.single.Replica
import me.aartikov.replica.single.ReplicaObserver

/**
 * Transforms [PagedReplica] to normal [Replica].
 */
fun <T : Any> PagedReplica<T>.toReplica(): Replica<T> {
    return PagedToNormalReplica(this)
}

fun <K : Any, T : Any> KeyedPagedReplica<K, T>.toReplica(): KeyedReplica<K, T> {
    return associate { key ->
        withKey(key).toReplica()
    }
}

private class PagedToNormalReplica<T : Any>(
    private val originalPagedReplica: PagedReplica<T>
) : Replica<T> {

    override fun observe(observerHost: ReplicaObserverHost): ReplicaObserver<T> {
        val originalObserver = originalPagedReplica.observe(observerHost)
        return PagedToNormalReplicaObserver(observerHost, originalObserver)
    }

    override fun refresh() {
        originalPagedReplica.refresh()
    }

    override fun revalidate() {
        originalPagedReplica.revalidate()
    }

    override suspend fun getData(forceRefresh: Boolean): T {
        // TODO: what to do with this?
        throw UnsupportedOperationException()
    }
}

private class PagedToNormalReplicaObserver<T : Any>(
    observerHost: ReplicaObserverHost,
    private val originalObserver: PagedReplicaObserver<T>
) : ReplicaObserver<T> {

    private val coroutineScope = observerHost.observerCoroutineScope

    private val _stateFlow = MutableStateFlow(Loadable<T>())
    override val stateFlow: StateFlow<Loadable<T>> = _stateFlow.asStateFlow()

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
                _stateFlow.value = Loadable(
                    loading = state.loading,
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