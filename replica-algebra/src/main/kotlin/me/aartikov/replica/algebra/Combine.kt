package me.aartikov.replica.algebra

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import me.aartikov.replica.common.CombinedLoadingError
import me.aartikov.replica.common.LoadingError
import me.aartikov.replica.single.Loadable
import me.aartikov.replica.single.Replica
import me.aartikov.replica.single.ReplicaObserver

@Suppress("UNCHECKED_CAST")
fun <T1 : Any, T2 : Any, R : Any> combine(
    replica1: Replica<T1>,
    replica2: Replica<T2>,
    transform: (T1, T2) -> R
): Replica<R> {
    return CombinedReplica<R>(
        originalReplicas = listOf(replica1, replica2),
        transform = {
            transform(it[0] as T1, it[1] as T2)
        },
        eager = false
    )
}

@Suppress("UNCHECKED_CAST")
fun <T1 : Any, T2 : Any, R : Any> combineEager(
    replica1: Replica<T1>,
    replica2: Replica<T2>,
    transform: (T1?, T2?) -> R
): Replica<R> {
    return CombinedReplica<R>(
        originalReplicas = listOf(replica1, replica2),
        transform = {
            transform(it[0] as T1?, it[1] as T2?)
        },
        eager = true
    )
}

@Suppress("UNCHECKED_CAST")
fun <T1 : Any, T2 : Any, T3 : Any, R : Any> combine(
    replica1: Replica<T1>,
    replica2: Replica<T2>,
    replica3: Replica<T3>,
    transform: (T1, T2, T3) -> R
): Replica<R> {
    return CombinedReplica<R>(
        originalReplicas = listOf(replica1, replica2, replica3),
        transform = {
            transform(it[0] as T1, it[1] as T2, it[2] as T3)
        },
        eager = false
    )
}

@Suppress("UNCHECKED_CAST")
fun <T1 : Any, T2 : Any, T3 : Any, R : Any> combineEager(
    replica1: Replica<T1>,
    replica2: Replica<T2>,
    replica3: Replica<T3>,
    transform: (T1?, T2?, T3?) -> R
): Replica<R> {
    return CombinedReplica<R>(
        originalReplicas = listOf(replica1, replica2, replica3),
        transform = {
            transform(it[0] as T1?, it[1] as T2?, it[2] as T3?)
        },
        eager = true
    )
}

@Suppress("UNCHECKED_CAST")
fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any, R : Any> combine(
    replica1: Replica<T1>,
    replica2: Replica<T2>,
    replica3: Replica<T3>,
    replica4: Replica<T4>,
    transform: (T1, T2, T3, T4) -> R
): Replica<R> {
    return CombinedReplica<R>(
        originalReplicas = listOf(replica1, replica2, replica3, replica4),
        transform = {
            transform(it[0] as T1, it[1] as T2, it[2] as T3, it[3] as T4)
        },
        eager = false
    )
}

@Suppress("UNCHECKED_CAST")
fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any, R : Any> combineEager(
    replica1: Replica<T1>,
    replica2: Replica<T2>,
    replica3: Replica<T3>,
    replica4: Replica<T4>,
    transform: (T1?, T2?, T3?, T4?) -> R
): Replica<R> {
    return CombinedReplica<R>(
        originalReplicas = listOf(replica1, replica2, replica3, replica4),
        transform = {
            transform(it[0] as T1?, it[1] as T2?, it[2] as T3?, it[3] as T4?)
        },
        eager = true
    )
}

@Suppress("UNCHECKED_CAST")
fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any, T5 : Any, R : Any> combine(
    replica1: Replica<T1>,
    replica2: Replica<T2>,
    replica3: Replica<T3>,
    replica4: Replica<T4>,
    replica5: Replica<T5>,
    transform: (T1, T2, T3, T4, T5) -> R
): Replica<R> {
    return CombinedReplica<R>(
        originalReplicas = listOf(replica1, replica2, replica3, replica4, replica5),
        transform = {
            transform(it[0] as T1, it[1] as T2, it[2] as T3, it[3] as T4, it[4] as T5)
        },
        eager = false
    )
}

@Suppress("UNCHECKED_CAST")
fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any, T5 : Any, R : Any> combineEager(
    replica1: Replica<T1>,
    replica2: Replica<T2>,
    replica3: Replica<T3>,
    replica4: Replica<T4>,
    replica5: Replica<T5>,
    transform: (T1?, T2?, T3?, T4?, T5?) -> R
): Replica<R> {
    return CombinedReplica<R>(
        originalReplicas = listOf(replica1, replica2, replica3, replica4, replica5),
        transform = {
            transform(it[0] as T1?, it[1] as T2?, it[2] as T3?, it[3] as T4?, it[4] as T5?)
        },
        eager = true
    )
}

private class CombinedReplica<R : Any>(
    private val originalReplicas: List<Replica<Any>>,
    private val transform: (List<Any?>) -> R,
    private val eager: Boolean
) : Replica<R> {

    override fun observe(
        observerCoroutineScope: CoroutineScope,
        observerActive: StateFlow<Boolean>
    ): ReplicaObserver<R> {
        return CombinedReplicaObserver(
            observerCoroutineScope,
            observerActive,
            originalReplicas,
            transform,
            eager
        )
    }

    override fun refresh() {
        originalReplicas.forEach {
            it.refresh()
        }
    }

    override fun revalidate() {
        originalReplicas.forEach {
            it.revalidate()
        }
    }

    override suspend fun getData(): R = coroutineScope {
        val deferredResults = originalReplicas.map {
            async { it.getData() }
        }
        transform(deferredResults.map { it.await() })
    }

    override suspend fun getRefreshedData(): R = coroutineScope {
        val deferredResults = originalReplicas.map {
            async { it.getRefreshedData() }
        }
        transform(deferredResults.map { it.await() })
    }
}

private class CombinedReplicaObserver<R : Any>(
    private val coroutineScope: CoroutineScope,
    activeFlow: StateFlow<Boolean>,
    originalReplicas: List<Replica<Any>>,
    private val transform: (List<Any?>) -> R,
    private val eager: Boolean
) : ReplicaObserver<R> {

    private val _stateFlow = MutableStateFlow(Loadable<R>())
    override val stateFlow: StateFlow<Loadable<R>> = _stateFlow.asStateFlow()

    private val _loadingErrorFlow = MutableSharedFlow<LoadingError>()
    override val loadingErrorFlow: Flow<LoadingError> = _loadingErrorFlow.asSharedFlow()

    val originalObservers = originalReplicas.map {
        it.observe(coroutineScope, activeFlow)
    }

    private var stateObservingJob: Job? = null
    private var errorsObservingJobs: List<Job> = emptyList()

    init {
        if (coroutineScope.isActive) {
            launchStateObserving()
            launchLoadingErrorsObserving()
        }
    }

    private fun launchStateObserving() {
        stateObservingJob = combine(
            originalObservers.map { it.stateFlow },
            transform = { it.asList() }
        )
            .onEach { states ->
                val combinedLoading = states.any { it.loading }
                var combinedData: R? = null
                var combiningError: Exception? = null

                try {
                    combinedData = if (eager) {
                        val dataList = states.map { it.data }
                        if (dataList.any { it != null }) transform(dataList) else null
                    } else {
                        val dataList = states.mapNotNull { it.data }
                        if (dataList.size == states.size) transform(dataList) else null
                    }
                } catch (e: Exception) {
                    combiningError = e
                }

                _stateFlow.value = Loadable(
                    loading = combinedLoading,
                    data = combinedData,
                    error = if (combiningError != null) {
                        CombinedLoadingError(combiningError)
                    } else {
                        val exceptions = states.mapNotNull { it.error }.flatMap { it.exceptions }
                        if (exceptions.isNotEmpty()) {
                            CombinedLoadingError(exceptions)
                        } else {
                            null
                        }
                    }
                )

                if (combiningError != null && !combinedLoading) {
                    _loadingErrorFlow.emit(LoadingError(combiningError))
                }
            }
            .launchIn(coroutineScope)
    }

    private fun launchLoadingErrorsObserving() {
        errorsObservingJobs = originalObservers.map { observer ->
            observer.loadingErrorFlow
                .onEach { error ->
                    _loadingErrorFlow.emit(error)
                }
                .launchIn(coroutineScope)
        }
    }

    override fun cancelObserving() {
        originalObservers.forEach { observer ->
            observer.cancelObserving()
        }

        stateObservingJob?.cancel()
        stateObservingJob = null

        errorsObservingJobs.forEach { job ->
            job.cancel()
        }
        errorsObservingJobs = emptyList()
    }
}