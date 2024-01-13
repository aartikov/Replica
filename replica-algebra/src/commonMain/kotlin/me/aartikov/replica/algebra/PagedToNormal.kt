package me.aartikov.replica.algebra

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import me.aartikov.replica.common.LoadingError
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedData
import me.aartikov.replica.paged.PagedReplica
import me.aartikov.replica.paged.PagedReplicaObserver
import me.aartikov.replica.single.Loadable
import me.aartikov.replica.single.Replica
import me.aartikov.replica.single.ReplicaObserver

/**
 * Transforms [PagedReplica] to normal [Replica].
 */
fun <T : Any, P : Page<T>> PagedReplica<T, P>.toReplica(): Replica<PagedData<T, P>> {
    return PagedToNormalReplica(this)
}

private class PagedToNormalReplica<T : Any, P : Page<T>>(
    private val originalPagedReplica: PagedReplica<T, P>
) : Replica<PagedData<T, P>> {

    override fun observe(
        observerCoroutineScope: CoroutineScope,
        observerActive: StateFlow<Boolean>
    ): ReplicaObserver<PagedData<T, P>> {
        val originalObserver = originalPagedReplica.observe(
            observerCoroutineScope,
            observerActive
        )

        return PagedToNormalReplicaObserver(
            observerCoroutineScope,
            originalObserver
        )
    }

    override fun refresh() {
        originalPagedReplica.refresh()
    }

    override fun revalidate() {
        originalPagedReplica.revalidate()
    }

    override suspend fun getData(forceRefresh: Boolean): PagedData<T, P> {
        // TODO: what to do with this?
        throw UnsupportedOperationException()
    }
}

private class PagedToNormalReplicaObserver<T : Any, P : Page<T>>(
    private val coroutineScope: CoroutineScope,
    private val originalObserver: PagedReplicaObserver<T, P>
) : ReplicaObserver<PagedData<T, P>> {

    private val _stateFlow = MutableStateFlow(Loadable<PagedData<T, P>>())
    override val stateFlow: StateFlow<Loadable<PagedData<T, P>>> = _stateFlow.asStateFlow()

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