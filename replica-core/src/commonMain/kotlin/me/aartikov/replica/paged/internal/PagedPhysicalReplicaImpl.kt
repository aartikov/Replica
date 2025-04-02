package me.aartikov.replica.paged.internal

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.common.InvalidationMode
import me.aartikov.replica.common.OptimisticUpdate
import me.aartikov.replica.common.ReplicaId
import me.aartikov.replica.common.ReplicaObserverHost
import me.aartikov.replica.common.ReplicaTag
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedData
import me.aartikov.replica.paged.PagedFetcher
import me.aartikov.replica.paged.PagedPhysicalReplica
import me.aartikov.replica.paged.PagedReplicaEvent
import me.aartikov.replica.paged.PagedReplicaObserver
import me.aartikov.replica.paged.PagedReplicaSettings
import me.aartikov.replica.paged.PagedReplicaState
import me.aartikov.replica.paged.behaviour.PagedReplicaBehaviour
import me.aartikov.replica.paged.internal.controllers.ClearingController
import me.aartikov.replica.paged.internal.controllers.DataChangingController
import me.aartikov.replica.paged.internal.controllers.DataLoadingController
import me.aartikov.replica.paged.internal.controllers.FreshnessController
import me.aartikov.replica.paged.internal.controllers.ObserversController
import me.aartikov.replica.paged.internal.controllers.OptimisticUpdatesController
import me.aartikov.replica.time.TimeProvider


internal class PagedPhysicalReplicaImpl<I : Any, P : Page<I>>(
    replicaClient: ReplicaClient,
    timeProvider: TimeProvider,
    dispatcher: CoroutineDispatcher,
    override val coroutineScope: CoroutineScope,
    override val name: String,
    override val settings: PagedReplicaSettings,
    override val tags: Set<ReplicaTag>,
    idExtractor: ((I) -> Any)?,
    behaviours: List<PagedReplicaBehaviour<I, P>>,
    fetcher: PagedFetcher<I, P>
) : PagedPhysicalReplica<I, P> {

    override val id: ReplicaId = ReplicaId.random()

    private val _stateFlow = MutableStateFlow(PagedReplicaState.createEmpty<I, P>())
    override val stateFlow: StateFlow<PagedReplicaState<I, P>> = _stateFlow.asStateFlow()

    private val _eventFlow = MutableSharedFlow<PagedReplicaEvent<I, P>>(extraBufferCapacity = 1000)
    override val eventFlow: Flow<PagedReplicaEvent<I, P>> = _eventFlow.asSharedFlow()

    private val observersController =
        ObserversController(timeProvider, dispatcher, _stateFlow, _eventFlow)

    private val dataLoadingController = DataLoadingController(
        coroutineScope, timeProvider, dispatcher, idExtractor, _stateFlow, _eventFlow,
        DataLoader(coroutineScope, fetcher)
    )

    private val dataChangingController =
        DataChangingController(timeProvider, dispatcher, idExtractor, _stateFlow)

    private val freshnessController = FreshnessController(dispatcher, _stateFlow, _eventFlow)

    private val clearingController = ClearingController(dispatcher, _stateFlow, _eventFlow)

    private val optimisticUpdatesController =
        OptimisticUpdatesController(timeProvider, dispatcher, idExtractor, _stateFlow)

    init {
        behaviours.forEach { behaviour ->
            behaviour.setup(replicaClient, this)
        }
    }

    override fun observe(observerHost: ReplicaObserverHost): PagedReplicaObserver<PagedData<I, P>> {
        return PagedReplicaObserverImpl(
            observerHost = observerHost,
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

    override fun loadNext() {
        coroutineScope.launch {
            dataLoadingController.loadNext()
        }
    }

    override fun loadPrevious() {
        coroutineScope.launch {
            dataLoadingController.loadPrevious()
        }
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
        coroutineScope.launch {
            dataLoadingController.cancel()
        }
    }

    override suspend fun clear(invalidationMode: InvalidationMode) {
        dataLoadingController.cancel()
        clearingController.clear()
        dataLoadingController.refreshAfterInvalidation(invalidationMode)
    }

    override suspend fun clearError() {
        clearingController.clearError()
    }

    override suspend fun beginOptimisticUpdate(
        update: OptimisticUpdate<List<P>>,
        operationId: Any
    ) {
        optimisticUpdatesController.beginOptimisticUpdate(update, operationId)
    }

    override suspend fun commitOptimisticUpdate(operationId: Any) {
        optimisticUpdatesController.commitOptimisticUpdate(operationId)
    }

    override suspend fun rollbackOptimisticUpdate(operationId: Any) {
        optimisticUpdatesController.rollbackOptimisticUpdate(operationId)
    }
}