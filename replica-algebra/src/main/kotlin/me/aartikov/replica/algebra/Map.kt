package me.aartikov.replica.algebra

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import me.aartikov.replica.common.CombinedLoadingError
import me.aartikov.replica.common.LoadingError
import me.aartikov.replica.single.Loadable
import me.aartikov.replica.single.Replica
import me.aartikov.replica.single.ReplicaObserver

fun <T : Any, R : Any> Replica<T>.map(transform: (T) -> R): Replica<R> {
    return MappedReplica(this, transform)
}

private class MappedReplica<T : Any, R : Any>(
    private val originalReplica: Replica<T>,
    private val transform: (T) -> R
) : Replica<R> {

    override fun observe(
        observerCoroutineScope: CoroutineScope,
        observerActive: StateFlow<Boolean>
    ): ReplicaObserver<R> {
        return MappedReplicaObserver(
            observerCoroutineScope,
            observerActive,
            originalReplica,
            transform
        )
    }

    override fun refresh() {
        originalReplica.refresh()
    }

    override fun revalidate() {
        originalReplica.revalidate()
    }

    override suspend fun getData(): R {
        return originalReplica.getData().let(transform)
    }

    override suspend fun getRefreshedData(): R {
        return originalReplica.getRefreshedData().let(transform)
    }
}

private class MappedReplicaObserver<T : Any, R : Any>(
    private val coroutineScope: CoroutineScope,
    activeFlow: StateFlow<Boolean>,
    originalReplica: Replica<T>,
    private val transform: (T) -> R
) : ReplicaObserver<R> {

    private val _stateFlow = MutableStateFlow(Loadable<R>())
    override val stateFlow: StateFlow<Loadable<R>> = _stateFlow.asStateFlow()

    private val _loadingErrorFlow = MutableSharedFlow<LoadingError>()
    override val loadingErrorFlow: Flow<LoadingError> = _loadingErrorFlow.asSharedFlow()

    val originalObserver = originalReplica.observe(coroutineScope, activeFlow)

    private var stateObservingJob: Job? = null
    private var errorsObservingJob: Job? = null

    init {
        if (coroutineScope.isActive) {
            launchStateObserving()
            launchLoadingErrorsObserving()
        }
    }

    private fun launchStateObserving() {
        stateObservingJob = originalObserver.stateFlow
            .onEach { state ->
                var mappedData: R? = null
                var mappingError: Exception? = null

                try {
                    mappedData = state.data?.let(transform)
                } catch (e: Exception) {
                    mappingError = e
                }

                _stateFlow.value = Loadable(
                    loading = state.loading,
                    data = mappedData,
                    error = if (mappingError != null) {
                        CombinedLoadingError(mappingError)
                    } else {
                        state.error
                    }
                )

                if (mappingError != null && !state.loading) {
                    _loadingErrorFlow.emit(LoadingError(mappingError))
                }
            }
            .launchIn(coroutineScope)
    }

    private fun launchLoadingErrorsObserving() {
        errorsObservingJob = originalObserver.loadingErrorFlow
            .onEach { error ->
                _loadingErrorFlow.emit(error)
            }
            .launchIn(coroutineScope)
    }

    override fun cancelObserving() {
        originalObserver.cancelObserving()

        stateObservingJob?.cancel()
        stateObservingJob = null

        errorsObservingJob?.cancel()
        errorsObservingJob = null
    }
}