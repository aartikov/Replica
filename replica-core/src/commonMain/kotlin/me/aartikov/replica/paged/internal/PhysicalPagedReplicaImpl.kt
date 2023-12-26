package me.aartikov.replica.paged.internal

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import me.aartikov.replica.common.InvalidationMode
import me.aartikov.replica.common.OptimisticUpdate
import me.aartikov.replica.common.ReplicaId
import me.aartikov.replica.common.ReplicaTag
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedFetcher
import me.aartikov.replica.paged.PagedReplicaEvent
import me.aartikov.replica.paged.PagedReplicaObserver
import me.aartikov.replica.paged.PagedReplicaSettings
import me.aartikov.replica.paged.PagedReplicaState
import me.aartikov.replica.paged.PhysicalPagedReplica
import me.aartikov.replica.paged.behaviour.PagedReplicaBehaviour
import me.aartikov.replica.paged.internal.controllers.ClearingController
import me.aartikov.replica.paged.internal.controllers.DataChangingController
import me.aartikov.replica.paged.internal.controllers.DataLoadingController
import me.aartikov.replica.paged.internal.controllers.FreshnessController
import me.aartikov.replica.paged.internal.controllers.ObserversController
import me.aartikov.replica.paged.internal.controllers.OptimisticUpdatesController
import me.aartikov.replica.time.TimeProvider


internal class PhysicalPagedReplicaImpl<T : Any, P : Page<T>>(
    timeProvider: TimeProvider,
    dispatcher: CoroutineDispatcher,
    override val coroutineScope: CoroutineScope,
    override val name: String,
    override val settings: PagedReplicaSettings,
    override val tags: Set<ReplicaTag>,
    behaviours: List<PagedReplicaBehaviour<T, P>>,
    fetcher: PagedFetcher<T, P>
) : PhysicalPagedReplica<T, P> {

    override val id: ReplicaId = ReplicaId.random()

    private val _stateFlow = MutableStateFlow(PagedReplicaState.createEmpty<T, P>())
    override val stateFlow: StateFlow<PagedReplicaState<T, P>> = _stateFlow.asStateFlow()

    private val _eventFlow = MutableSharedFlow<PagedReplicaEvent<T, P>>(extraBufferCapacity = 1000)
    override val eventFlow: Flow<PagedReplicaEvent<T, P>> = _eventFlow.asSharedFlow()

    private val observersController =
        ObserversController(timeProvider, dispatcher, _stateFlow, _eventFlow)

    private val dataLoadingController = DataLoadingController(
        timeProvider, dispatcher, coroutineScope, _stateFlow, _eventFlow,
        DataLoader(coroutineScope, fetcher)
    )

    private val dataChangingController =
        DataChangingController(timeProvider, dispatcher, _stateFlow)

    private val freshnessController = FreshnessController(dispatcher, _stateFlow, _eventFlow)

    private val clearingController = ClearingController(dispatcher, _stateFlow, _eventFlow)

    private val optimisticUpdatesController =
        OptimisticUpdatesController(timeProvider, dispatcher, _stateFlow)

    init {
        behaviours.forEach { behaviour ->
            behaviour.setup(this)
        }
    }

    override fun observe(
        observerCoroutineScope: CoroutineScope,
        observerActive: StateFlow<Boolean>
    ): PagedReplicaObserver<T, P> {
        return PagedReplicaObserverImpl(
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

    override suspend fun setData(data: List<P>) {
        dataChangingController.setData(data)
    }

    override suspend fun mutateData(transform: (List<P>) -> List<P>) {
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

    override suspend fun clear() {
        cancel()
        clearingController.clear()
    }

    override suspend fun clearError() {
        clearingController.clearError()
    }

    override suspend fun beginOptimisticUpdate(update: OptimisticUpdate<List<P>>) {
        optimisticUpdatesController.beginOptimisticUpdate(update)
    }

    override suspend fun commitOptimisticUpdate(update: OptimisticUpdate<List<P>>) {
        optimisticUpdatesController.commitOptimisticUpdate(update)
    }

    override suspend fun rollbackOptimisticUpdate(update: OptimisticUpdate<List<P>>) {
        optimisticUpdatesController.rollbackOptimisticUpdate(update)
    }
}