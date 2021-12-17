package me.aartikov.replica.simple.internal

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import me.aartikov.replica.simple.*
import me.aartikov.replica.simple.ReplicaEvent.*
import me.aartikov.replica.simple.behaviour.ReplicaBehaviour
import me.aartikov.replica.simple.internal.Action.*

internal class ReplicaImpl<T : Any>(
    behaviours: List<ReplicaBehaviour<T>>,
    override val coroutineScope: CoroutineScope,
    fetcher: Fetcher<T>,
    onFinished: (ReplicaImpl<T>) -> Unit
) : CoreReplica<T> {

    private val _eventFlow = MutableSharedFlow<ReplicaEvent<T>>(extraBufferCapacity = 100)

    private val loop: ReplicaLoop<T> = ReplicaLoop(
        initialState = ReplicaState.createEmpty(),
        reducer = ReplicaReducer(),
        effectHandlers = listOf(
            LoadingEffectHandler(fetcher),
            EventEffectHandler { event -> _eventFlow.emit(event) }
        )
    )

    override val stateFlow: StateFlow<ReplicaState<T>>
        get() = loop.stateFlow

    override val eventFlow: Flow<ReplicaEvent<T>>
        get() = _eventFlow

    init {
        initBehaviours(behaviours)
        coroutineScope.launch {
            try {
                loop.start()
            } finally {
                onFinished(this@ReplicaImpl)
            }
        }

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
        observerActiveFlow: StateFlow<Boolean>
    ): ReplicaObserver<T> {

        return ReplicaObserverImpl(
            coroutineScope = observerCoroutineScope,
            activeFlow = observerActiveFlow,
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

    override fun invalidate() {
        makeStale()
        if (state.activeObserverCount > 0) {
            refresh()
        }
    }

    override fun cancelLoading() {
        loop.dispatch(LoadingAction.Cancel)
    }

    override fun clear() {
        loop.dispatch(ClearAction.ClearAll)
    }

    override fun clearError() {
        loop.dispatch(ClearAction.ClearError)
    }
}