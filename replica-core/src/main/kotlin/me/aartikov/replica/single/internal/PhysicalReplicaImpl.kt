package me.aartikov.replica.single.internal

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import me.aartikov.replica.single.*
import me.aartikov.replica.single.behaviour.ReplicaBehaviour
import me.aartikov.replica.single.internal.controllers.*
import me.aartikov.replica.time.TimeProvider


internal class PhysicalReplicaImpl<T : Any>(
    timeProvider: TimeProvider,
    dispatcher: CoroutineDispatcher,
    override val coroutineScope: CoroutineScope,
    override val name: String,
    override val settings: ReplicaSettings,
    behaviours: List<ReplicaBehaviour<T>>,
    storage: Storage<T>?,
    fetcher: Fetcher<T>
) : PhysicalReplica<T> {

    override val id: ReplicaId = ReplicaId.random()

    private val _stateFlow = MutableStateFlow(
        ReplicaState.createEmpty<T>(hasStorage = storage != null)
    )
    override val stateFlow: StateFlow<ReplicaState<T>> = _stateFlow.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ReplicaEvent<T>>(extraBufferCapacity = 1000)
    override val eventFlow: Flow<ReplicaEvent<T>> = _eventFlow.asSharedFlow()

    private val observersController =
        ObserversController(timeProvider, dispatcher, _stateFlow, _eventFlow)

    private val dataLoadingController = DataLoadingController(
        timeProvider, dispatcher, coroutineScope, _stateFlow, _eventFlow,
        DataLoader(coroutineScope, storage, fetcher)
    )

    private val dataChangingController =
        DataChangingController(timeProvider, dispatcher, _stateFlow, storage)

    private val freshnessController = FreshnessController(dispatcher, _stateFlow, _eventFlow)

    private val clearingController = ClearingController(dispatcher, _stateFlow, _eventFlow, storage)

    private val optimisticUpdatesController =
        OptimisticUpdatesController(timeProvider, dispatcher, _stateFlow, storage)

    init {
        behaviours.forEach { behaviour ->
            behaviour.setup(this)
        }
    }

    override fun observe(
        observerCoroutineScope: CoroutineScope,
        observerActive: StateFlow<Boolean>
    ): ReplicaObserver<T> {
        return ReplicaObserverImpl(
            coroutineScope = observerCoroutineScope,
            activeFlow = observerActive,
            replicaStateFlow = stateFlow,
            replicaEventFlow = eventFlow,
            observersController = observersController
        )
    }

    override fun refresh() {
        dataLoadingController.refresh()
    }

    override fun revalidate() {
        dataLoadingController.revalidate()
    }

    override suspend fun getData(): T {
        return dataLoadingController.getData()
    }

    override suspend fun getRefreshedData(): T {
        return dataLoadingController.getRefreshedData()
    }

    override suspend fun setData(data: T) {
        dataChangingController.setData(data)
    }

    override suspend fun mutateData(transform: (T) -> T) {
        dataChangingController.mutateData(transform)
    }

    override suspend fun invalidate(mode: InvalidationMode) {
        freshnessController.invalidate()
        dataLoadingController.refreshAfterInvalidation(mode)
    }

    override suspend fun makeFresh() {
        freshnessController.makeFresh()
    }

    override fun cancel() {
        dataLoadingController.cancel()
    }

    override suspend fun clear(removeFromStorage: Boolean) {
        cancel()
        clearingController.clear(removeFromStorage)
    }

    override suspend fun clearError() {
        clearingController.clearError()
    }

    override suspend fun beginOptimisticUpdate(update: OptimisticUpdate<T>) {
        optimisticUpdatesController.beginOptimisticUpdate(update)
    }

    override suspend fun commitOptimisticUpdate(update: OptimisticUpdate<T>) {
        optimisticUpdatesController.commitOptimisticUpdate(update)
    }

    override suspend fun rollbackOptimisticUpdate(update: OptimisticUpdate<T>) {
        optimisticUpdatesController.rollbackOptimisticUpdate(update)
    }
}