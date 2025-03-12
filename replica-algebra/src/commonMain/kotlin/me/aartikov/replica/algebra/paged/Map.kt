package me.aartikov.replica.algebra.paged

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import me.aartikov.replica.common.CombinedLoadingError
import me.aartikov.replica.common.LoadingError
import me.aartikov.replica.common.LoadingReason
import me.aartikov.replica.common.ReplicaObserverHost
import me.aartikov.replica.keyed_paged.KeyedPagedReplica
import me.aartikov.replica.paged.Paged
import me.aartikov.replica.paged.PagedLoadingStatus
import me.aartikov.replica.paged.PagedReplica
import me.aartikov.replica.paged.PagedReplicaObserver

/**
 * Transforms paged replica data with a [transform] function.
 */
fun <T : Any, R : Any> PagedReplica<T>.map(transform: (T) -> R): PagedReplica<R> {
    return MappedReplica(this, transform)
}

/**
 * Transforms keyed replica data with a [transform] function.
 */
fun <K : Any, T : Any, R : Any> KeyedPagedReplica<K, T>.map(transform: (K, T) -> R): KeyedPagedReplica<K, R> {
    return associatePaged { key ->
        withKey(key).map { transform(key, it) }
    }
}

private class MappedReplica<T : Any, R : Any>(
    private val originalReplica: PagedReplica<T>,
    private val transform: (T) -> R
) : PagedReplica<R> {

    override fun observe(observerHost: ReplicaObserverHost): PagedReplicaObserver<R> {
        val originalObserver = originalReplica.observe(observerHost)

        return MappedReplicaObserver(
            observerHost,
            originalObserver,
            transform
        )
    }

    override fun refresh() {
        originalReplica.refresh()
    }

    override fun revalidate() {
        originalReplica.revalidate()
    }

    override fun loadNext() {
        originalReplica.loadNext()
    }

    override fun loadPrevious() {
        originalReplica.loadPrevious()
    }
}

private class MappingResult<T : Any, R : Any>(
    val originalData: T?,
    val data: R?,
    val exception: Exception?
)

private class MappedReplicaObserver<T : Any, R : Any>(
    private val observerHost: ReplicaObserverHost,
    private val originalObserver: PagedReplicaObserver<T>,
    private val transform: (T) -> R
) : PagedReplicaObserver<R> {

    private val coroutineScope = observerHost.observerCoroutineScope

    private val _stateFlow = MutableStateFlow(Paged<R>())
    override val stateFlow: StateFlow<Paged<R>> = _stateFlow.asStateFlow()

    private val _loadingErrorFlow = MutableSharedFlow<LoadingError>()
    override val loadingErrorFlow: Flow<LoadingError> = _loadingErrorFlow.asSharedFlow()

    private var stateObservingJob: Job? = null
    private var errorsObservingJob: Job? = null

    init {
        if (coroutineScope.isActive) {
            launchStateObserving()
            launchLoadingErrorsObserving()
        }
    }

    private fun launchStateObserving() {
        var cachedMappingResult: MappingResult<T, R>? = null
        stateObservingJob = originalObserver.stateFlow
            .onEach { state ->
                val mappingResult = mapState(state, cachedMappingResult)
                cachedMappingResult = mappingResult

                _stateFlow.value = Paged(
                    loadingStatus = state.loadingStatus,
                    data = mappingResult.data,
                    error = if (mappingResult.exception != null) {
                        CombinedLoadingError(LoadingReason.Normal, mappingResult.exception)
                    } else {
                        state.error
                    }
                )

                if (mappingResult.exception != null && state.loadingStatus == PagedLoadingStatus.None) {
                    _loadingErrorFlow.emit(LoadingError(LoadingReason.Normal, mappingResult.exception))
                }
            }
            .launchIn(coroutineScope)
    }

    private fun mapState(
        state: Paged<T>,
        cachedResult: MappingResult<T, R>?
    ): MappingResult<T, R> {
        val originalData = state.data
        if (cachedResult != null && originalData == cachedResult.originalData) {
            return cachedResult
        }

        return try {
            val mappedData = originalData?.let(transform)
            MappingResult(originalData, mappedData, null)
        } catch (e: Exception) {
            MappingResult(originalData, null, e)
        }
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