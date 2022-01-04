package me.aartikov.replica.single.internal

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import me.aartikov.replica.single.*
import me.aartikov.replica.single.behaviour.ReplicaBehaviour
import me.aartikov.replica.single.internal.controllers.*


internal class PhysicalReplicaImpl<T : Any>(
    private val dispatcher: CoroutineDispatcher,
    private val coroutineScope: CoroutineScope,
    behaviours: List<ReplicaBehaviour<T>>,
    storage: Storage<T>?,
    fetcher: Fetcher<T>
) : PhysicalReplica<T> {

    private val _stateFlow = MutableStateFlow(
        ReplicaState.createEmpty<T>(hasStorage = storage != null)
    )
    override val stateFlow: StateFlow<ReplicaState<T>> = _stateFlow.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ReplicaEvent<T>>()
    override val eventFlow: Flow<ReplicaEvent<T>> = _eventFlow.asSharedFlow()

    private val observersController = ObserversController(dispatcher, _stateFlow, _eventFlow)

    private val dataLoadingController = DataLoadingController(
        dispatcher,
        coroutineScope,
        _stateFlow,
        _eventFlow,
        DataLoader(coroutineScope, storage, fetcher)
    )

    private val dataChangingController = DataChangingController(dispatcher, _stateFlow, storage)

    private val freshnessController = FreshnessController(dispatcher, _stateFlow, _eventFlow)

    private val clearingController = ClearingController(dispatcher, _stateFlow, _eventFlow, storage)

    init {
        behaviours.forEach { behaviour ->
            behaviour.setup(coroutineScope, this)
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

    override fun cancelLoading() {
        dataLoadingController.cancelLoading()
    }

    override suspend fun setData(data: T) {
        dataChangingController.setData(data)
    }

    override suspend fun mutateData(transform: (T) -> T) {
        dataChangingController.mutateData(transform)
    }

    override suspend fun invalidate(refreshCondition: RefreshCondition) {
        freshnessController.invalidate()
        dataLoadingController.refresh(refreshCondition)
    }

    override suspend fun makeFresh() {
        freshnessController.makeFresh()
    }

    override suspend fun clear(removeFromStorage: Boolean) {
        cancelLoading()
        clearingController.clear(removeFromStorage)
    }

    override suspend fun clearError() {
        clearingController.clearError()
    }
}