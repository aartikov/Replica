package me.aartikov.replica.algebra

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import me.aartikov.replica.common.CombinedLoadingError
import me.aartikov.replica.common.LoadingError
import me.aartikov.replica.single.Loadable
import me.aartikov.replica.single.Replica
import me.aartikov.replica.single.ReplicaObserver

/**
 * Combines two replicas to a single replica. Data is merged only when all replicas have loaded data.
 * @param replica1 first replica
 * @param replica2 second replica
 * @param transform function that merges replica data together.
 */
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

/**
 * Combines two replicas to a single replica. Data is merged when any of replicas has loaded data.
 * @param replica1 first replica
 * @param replica2 second replica
 * @param transform function that merges replica data together.
 */
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

/**
 * Combines three replicas to a single replica. Data is merged only when all replicas have loaded data.
 * @param replica1 first replica
 * @param replica2 second replica
 * @param replica3 third replica
 * @param transform function that merges replica data together.
 */
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

/**
 * Combines three replicas to a single replica. Data is merged when any of replicas has loaded data.
 * @param replica1 first replica
 * @param replica2 second replica
 * @param replica3 third replica
 * @param transform function that merges replica data together.
 */
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

/**
 * Combines four replicas to a single replica. Data is merged only when all replicas have loaded data.
 * @param replica1 first replica
 * @param replica2 second replica
 * @param replica3 third replica
 * @param replica4 fourth replica
 * @param transform function that merges replica data together.
 */
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

/**
 * Combines four replicas to a single replica. Data is merged when any of replicas has loaded data.
 * @param replica1 first replica
 * @param replica2 second replica
 * @param replica3 third replica
 * @param replica4 fourth replica
 * @param transform function that merges replica data together.
 */
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

/**
 * Combines five replicas to a single replica. Data is merged only when all replicas have loaded data.
 * @param replica1 first replica
 * @param replica2 second replica
 * @param replica3 third replica
 * @param replica4 fourth replica
 * @param replica5 fifth replica
 * @param transform function that merges replica data together.
 */
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

/**
 * Combines five replicas to a single replica. Data is merged when any of replicas has loaded data.
 * @param replica1 first replica
 * @param replica2 second replica
 * @param replica3 third replica
 * @param replica4 fourth replica
 * @param replica5 fifth replica
 * @param transform function that merges replica data together.
 */
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

/**
 * Combines list of replicas with same key type to a single replica. Data is merged only when all replicas have loaded data.
 * @param replicas list of replicas
 * @param transform function that merges replica data together.
 */
@Suppress("UNCHECKED_CAST")
fun <T : Any, R : Any> combine(
    replicas: List<Replica<T>>,
    transform: (List<T?>) -> R
): Replica<R> {
    return CombinedReplica<R>(
        originalReplicas = replicas,
        transform = { list ->
            transform(
                list.map { it as T? }
            )
        },
        eager = false
    )
}

/**
 * Combines list of replicas with same key type to a single replica. Data is merged only when all replicas have loaded data.
 * @param replicas list of replicas
 * @param transform function that merges replica data together.
 */
@Suppress("UNCHECKED_CAST")
fun <T : Any, R : Any> combineEager(
    replicas: List<Replica<T>>,
    transform: (List<T?>) -> R
): Replica<R> {
    return CombinedReplica<R>(
        originalReplicas = replicas,
        transform = { list ->
            transform(
                list.map { it as T? }
            )
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
        val originalObservers = originalReplicas.map {
            it.observe(observerCoroutineScope, observerActive)
        }

        return CombinedReplicaObserver(
            observerCoroutineScope,
            originalObservers,
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

    override suspend fun getData(forceRefresh: Boolean): R = coroutineScope {
        val deferredResults = originalReplicas.map {
            async { it.getData(forceRefresh) }
        }
        transform(deferredResults.map { it.await() })
    }
}

private class CombiningResult<R : Any>(
    val originalData: List<Any?>,
    val data: R?,
    val error: Exception?
)

private class CombinedReplicaObserver<R : Any>(
    private val coroutineScope: CoroutineScope,
    private val originalObservers: List<ReplicaObserver<Any>>,
    private val transform: (List<Any?>) -> R,
    private val eager: Boolean
) : ReplicaObserver<R> {

    private val _stateFlow = MutableStateFlow(Loadable<R>())
    override val stateFlow: StateFlow<Loadable<R>> = _stateFlow.asStateFlow()

    private val _loadingErrorFlow = MutableSharedFlow<LoadingError>()
    override val loadingErrorFlow: Flow<LoadingError> = _loadingErrorFlow.asSharedFlow()

    private var stateObservingJob: Job? = null
    private var errorsObservingJobs: List<Job> = emptyList()

    private val delayedLoadingErrors = mutableListOf<LoadingError>()

    init {
        if (coroutineScope.isActive) {
            launchStateObserving()
            launchLoadingErrorsObserving()
        }
    }

    private fun launchStateObserving() {
        var cachedCombiningResult: CombiningResult<R>? = null
        stateObservingJob = combine(
            originalObservers.map { it.stateFlow },
            transform = { it.asList() }
        )
            .onEach { states ->
                val combiningResult = combineStates(states, eager, cachedCombiningResult)
                cachedCombiningResult = combiningResult
                val combinedLoading = states.any { it.loading }

                _stateFlow.value = Loadable(
                    loading = combinedLoading,
                    data = combiningResult.data,
                    error = if (combiningResult.error != null) {
                        CombinedLoadingError(combiningResult.error)
                    } else {
                        val exceptions = states.mapNotNull { it.error }.flatMap { it.exceptions }
                        if (exceptions.isNotEmpty()) {
                            CombinedLoadingError(exceptions)
                        } else {
                            null
                        }
                    }
                )

                if (!combinedLoading) {
                    delayedLoadingErrors.forEach {
                        _loadingErrorFlow.emit(it)
                    }
                    delayedLoadingErrors.clear()

                    combiningResult.error?.let {
                        _loadingErrorFlow.emit(LoadingError(it))
                    }
                }
            }
            .launchIn(coroutineScope)
    }

    private fun launchLoadingErrorsObserving() {
        errorsObservingJobs = originalObservers.map { observer ->
            observer.loadingErrorFlow
                .onEach { error ->
                    if (_stateFlow.value.loading) {
                        delayedLoadingErrors.add(error)
                    } else {
                        _loadingErrorFlow.emit(error)
                    }
                }
                .launchIn(coroutineScope)
        }
    }

    private fun combineStates(
        states: List<Loadable<Any>>,
        eager: Boolean,
        cachedResult: CombiningResult<R>?
    ): CombiningResult<R> {
        val originalData = if (eager) {
            states.map { it.data }
        } else {
            states.mapNotNull { it.data }
        }

        if (originalData == cachedResult?.originalData) {
            return cachedResult
        }

        return try {
            val combinedData = if (eager) {
                if (originalData.any { it != null }) transform(originalData) else null
            } else {
                if (originalData.size == states.size) transform(originalData) else null
            }
            CombiningResult(originalData, combinedData, null)
        } catch (e: Exception) {
            CombiningResult(originalData, null, e)
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