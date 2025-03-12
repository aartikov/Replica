package me.aartikov.replica.single.internal

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.aartikov.replica.common.InvalidationMode
import me.aartikov.replica.common.OptimisticUpdate
import me.aartikov.replica.common.ReplicaId
import me.aartikov.replica.common.ReplicaTag
import me.aartikov.replica.single.Fetcher
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaEvent
import me.aartikov.replica.single.ReplicaObserver
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.single.ReplicaState
import me.aartikov.replica.single.Storage
import me.aartikov.replica.single.behaviour.ReplicaBehaviour
import me.aartikov.replica.single.internal.controllers.ClearingController
import me.aartikov.replica.single.internal.controllers.DataChangingController
import me.aartikov.replica.single.internal.controllers.DataLoadingController
import me.aartikov.replica.single.internal.controllers.FreshnessController
import me.aartikov.replica.single.internal.controllers.ObserversController
import me.aartikov.replica.single.internal.controllers.OptimisticUpdatesController
import me.aartikov.replica.time.TimeProvider


internal class PhysicalReplicaImpl<T : Any>(
    timeProvider: TimeProvider,
    dispatcher: CoroutineDispatcher,
    override val coroutineScope: CoroutineScope,
    override val name: String,
    override val settings: ReplicaSettings,
    override val tags: Set<ReplicaTag>,
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
        coroutineScope, timeProvider, dispatcher, _stateFlow, _eventFlow,
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
        coroutineScope.launch {
            dataLoadingController.refresh()
        }
    }

    override fun revalidate() {
        coroutineScope.launch {
            dataLoadingController.revalidate()
        }
    }

    override suspend fun getData(forceRefresh: Boolean): T {
        return dataLoadingController.getData(forceRefresh)
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
        coroutineScope.launch {
            dataLoadingController.cancel()
        }
    }

    override suspend fun clear(invalidationMode: InvalidationMode, removeFromStorage: Boolean) {
        dataLoadingController.cancel()
        clearingController.clear(removeFromStorage)
        dataLoadingController.refreshAfterInvalidation(invalidationMode)
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