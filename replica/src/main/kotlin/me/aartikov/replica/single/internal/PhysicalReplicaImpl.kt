package me.aartikov.replica.single.internal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import me.aartikov.replica.single.*
import me.aartikov.replica.single.ReplicaEvent.LoadingEvent
import me.aartikov.replica.single.behaviour.ReplicaBehaviour
import me.aartikov.replica.single.internal.Action.*
import me.aartikov.sesame.loop.startIn

internal class PhysicalReplicaImpl<T : Any>(
    override val coroutineScope: CoroutineScope,
    behaviours: List<ReplicaBehaviour<T>>,
    storage: Storage<T>?,
    fetcher: Fetcher<T>
) : PhysicalReplica<T> {

    private val _eventFlow = MutableSharedFlow<ReplicaEvent<T>>(extraBufferCapacity = 100)

    private val loop: ReplicaLoop<T> = ReplicaLoop(
        initialState = ReplicaState.createEmpty(hasStorage = storage != null),
        reducer = ReplicaReducer(),
        effectHandlers = listOf(
            LoadingEffectHandler(storage, fetcher),
            EventEffectHandler { event -> _eventFlow.emit(event) }
        )
    )

    override val stateFlow: StateFlow<ReplicaState<T>>
        get() = loop.stateFlow

    override val eventFlow: Flow<ReplicaEvent<T>>
        get() = _eventFlow

    init {
        initBehaviours(behaviours)
        loop.startIn(coroutineScope)
    }

    private fun initBehaviours(behaviours: List<ReplicaBehaviour<T>>) {
        behaviours.forEach { behaviour ->
            behaviour.setup(this)
        }
        eventFlow
            .onEach { event ->
                behaviours.forEach { behaviour ->
                    behaviour.handleEvent(this, event)
                }
            }
            .launchIn(coroutineScope)
    }

    override fun observe(
        observerCoroutineScope: CoroutineScope,
        observerActive: StateFlow<Boolean>
    ): ReplicaObserver<T> {

        return ReplicaObserverImpl(
            coroutineScope = observerCoroutineScope,
            activeFlow = observerActive,
            replicaStateFlow = loop.stateFlow,
            replicaEventFlow = eventFlow,
            dispatchAction = loop::dispatch
        )
    }

    override fun refresh() {
        loop.dispatch(LoadingAction.Load())
    }

    override fun revalidate() {
        if (!state.hasFreshData) {
            loop.dispatch(LoadingAction.Load())
        }
    }

    override suspend fun getData(): T {
        return getDataInternal(refreshed = false)
    }

    override suspend fun getRefreshedData(): T {
        return getDataInternal(refreshed = true)
    }

    private suspend fun getDataInternal(refreshed: Boolean): T {
        val data = state.data
        if (!refreshed && data != null && data.fresh) {
            return data.value
        }

        val event = eventFlow
            .onStart {
                loop.dispatch(LoadingAction.Load(dataRequested = true))
            }
            .filterIsInstance<LoadingEvent.LoadingFinished<T>>()
            .first()

        when (event) {
            is LoadingEvent.LoadingFinished.Success -> return event.data
            is LoadingEvent.LoadingFinished.Canceled -> throw CancellationException()
            is LoadingEvent.LoadingFinished.Error -> throw event.error
        }
    }

    override fun setData(data: T) {
        loop.dispatch(DataChangingAction.SetData(data))
    }

    override fun mutateData(transform: (T) -> T) {
        loop.dispatch(DataChangingAction.MutateData(transform))
    }

    override fun makeFresh() {
        loop.dispatch(FreshnessChangingAction.MakeFresh)
    }

    override fun makeStale() {
        loop.dispatch(FreshnessChangingAction.MakeStale)
    }

    override fun cancelLoading() {
        loop.dispatch(LoadingAction.Cancel)
    }

    override fun clear() {
        loop.dispatch(ClearAction.Clear)
    }

    override fun clearError() {
        loop.dispatch(ClearAction.ClearError)
    }
}