package me.aartikov.replica.keyed_paged.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import me.aartikov.replica.common.InvalidationMode
import me.aartikov.replica.common.OptimisticUpdate
import me.aartikov.replica.common.ReplicaId
import me.aartikov.replica.common.ReplicaTag
import me.aartikov.replica.common.internal.Lock
import me.aartikov.replica.common.internal.withLock
import me.aartikov.replica.keyed_paged.KeyedPagedPhysicalReplica
import me.aartikov.replica.keyed_paged.KeyedPagedReplicaEvent
import me.aartikov.replica.keyed_paged.KeyedPagedReplicaSettings
import me.aartikov.replica.keyed_paged.KeyedPagedReplicaState
import me.aartikov.replica.keyed_paged.behaviour.KeyedPagedReplicaBehaviour
import me.aartikov.replica.keyed_paged.internal.controllers.ChildRemovingController
import me.aartikov.replica.keyed_paged.internal.controllers.ObserverCountController
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedData
import me.aartikov.replica.paged.PagedPhysicalReplica
import me.aartikov.replica.paged.PagedReplicaObserver
import me.aartikov.replica.paged.PagedReplicaState
import me.aartikov.replica.paged.currentState

internal class KeyedPagedPhysicalReplicaImpl<K : Any, I : Any, P : Page<I>>(
    override val coroutineScope: CoroutineScope,
    override val name: String,
    override val settings: KeyedPagedReplicaSettings<K, I, P>,
    override val tags: Set<ReplicaTag>,
    behaviours: List<KeyedPagedReplicaBehaviour<K, I, P>>,
    private val replicaFactory: (CoroutineScope, K) -> PagedPhysicalReplica<I, P>
) : KeyedPagedPhysicalReplica<K, I, P> {

    override val id: ReplicaId = ReplicaId.random()

    private val _stateFlow = MutableStateFlow(KeyedPagedReplicaState.Empty)
    override val stateFlow get() = _stateFlow.asStateFlow()

    private val _eventFlow = MutableSharedFlow<KeyedPagedReplicaEvent<K, I, P>>(
        extraBufferCapacity = 1000
    )
    override val eventFlow get() = _eventFlow.asSharedFlow()

    private val replicasLock = Lock()
    private val replicas = mutableMapOf<K, PagedPhysicalReplica<I, P>>()

    private val childRemovingController = ChildRemovingController<K, I, P>(this::removeReplica)
    private val observerCountController = ObserverCountController<I, P>(_stateFlow)

    init {
        behaviours.forEach { behaviour ->
            behaviour.setup(this)
        }
    }

    override fun observe(
        observerCoroutineScope: CoroutineScope,
        observerActive: StateFlow<Boolean>,
        key: StateFlow<K?>
    ): PagedReplicaObserver<PagedData<I, P>> {
        return KeyedPagedReplicaObserverImpl(
            coroutineScope = observerCoroutineScope,
            activeFlow = observerActive,
            key = key,
            replicaProvider = { getOrCreateReplica(it) }
        )
    }

    override fun refresh(key: K) {
        getOrCreateReplica(key).refresh()
    }

    override fun revalidate(key: K) {
        getOrCreateReplica(key).revalidate()
    }

    override fun loadNext(key: K) {
        getOrCreateReplica(key).loadNext()
    }

    override fun loadPrevious(key: K) {
        getOrCreateReplica(key).loadPrevious()
    }

    override fun getCurrentState(key: K): PagedReplicaState<I, P>? {
        return getReplica(key)?.currentState
    }

    override suspend fun setData(key: K, data: List<P>) {
        getOrCreateReplica(key).setData(data)
    }

    override suspend fun mutateData(key: K, transform: (List<P>) -> List<P>) {
        getReplica(key)?.mutateData(transform)
    }

    override suspend fun invalidate(key: K, mode: InvalidationMode) {
        val replica = if (mode == InvalidationMode.RefreshAlways) {
            getOrCreateReplica(key)
        } else {
            getReplica(key)
        }
        replica?.invalidate(mode)
    }

    override suspend fun makeFresh(key: K) {
        getReplica(key)?.makeFresh()
    }

    override fun cancel(key: K) {
        getReplica(key)?.cancel()
    }

    override suspend fun clear(key: K) {
        getReplica(key)?.clear()
    }

    override suspend fun clearError(key: K) {
        getReplica(key)?.clearError()
    }

    override suspend fun clearAll() {
        onEachPagedReplica {
            clear()
        }
    }

    override suspend fun beginOptimisticUpdate(key: K, update: OptimisticUpdate<List<P>>) {
        getReplica(key)?.beginOptimisticUpdate(update)
    }

    override suspend fun commitOptimisticUpdate(key: K, update: OptimisticUpdate<List<P>>) {
        getReplica(key)?.commitOptimisticUpdate(update)
    }

    override suspend fun rollbackOptimisticUpdate(key: K, update: OptimisticUpdate<List<P>>) {
        getReplica(key)?.rollbackOptimisticUpdate(update)
    }

    override suspend fun onPagedReplica(
        key: K,
        action: suspend PagedPhysicalReplica<I, P>.() -> Unit
    ) {
        getOrCreateReplica(key).apply { action() }
    }

    override suspend fun onExistingPagedReplica(
        key: K, action:
        suspend PagedPhysicalReplica<I, P>.() -> Unit
    ) {
        getReplica(key)?.apply { action() }
    }

    override suspend fun onEachPagedReplica(
        action: suspend PagedPhysicalReplica<I, P>.(K) -> Unit
    ) {
        // make a copy for concurrent modification
        val replicasCopy = replicasLock.withLock {
            ArrayList(replicas.entries)
        }
        replicasCopy.forEach { (key, replica) ->
            replica.action(key)
        }
    }

    private fun getReplica(key: K): PagedPhysicalReplica<I, P>? = replicasLock.withLock {
        return replicas[key]
    }

    private fun getOrCreateReplica(key: K): PagedPhysicalReplica<I, P> {
        var created = false
        val replica = replicasLock.withLock {
            replicas.getOrPut(key) {
                createReplica(key).also {
                    created = true
                }
            }
        }

        if (created) {
            _stateFlow.update { state ->
                state.copy(replicaCount = state.replicaCount + 1)
            }
            _eventFlow.tryEmit(KeyedPagedReplicaEvent.ReplicaCreated(key, replica))
        }

        return replica
    }

    private fun createReplica(key: K): PagedPhysicalReplica<I, P> {
        val childCoroutineScope = coroutineScope.createChildScope()
        val replica = replicaFactory(childCoroutineScope, key)
        childRemovingController.setupAutoRemoving(key, replica)
        observerCountController.setupObserverCounting(replica)
        return replica
    }

    private fun removeReplica(key: K) {
        val removedReplica = replicasLock.withLock {
            replicas.remove(key)
        }

        if (removedReplica != null) {
            removedReplica.coroutineScope.cancel()
            _stateFlow.update { state ->
                // don't change replicaWithObserversCount and replicaWithActiveObserversCount
                // because only replicas with ObservingStatus.None can be removed
                state.copy(replicaCount = state.replicaCount - 1)
            }
            _eventFlow.tryEmit(KeyedPagedReplicaEvent.ReplicaRemoved(key, removedReplica.id))
        }
    }
}

private fun CoroutineScope.createChildScope(): CoroutineScope {
    return CoroutineScope(coroutineContext + SupervisorJob(parent = coroutineContext[Job]))
}