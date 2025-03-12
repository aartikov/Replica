package me.aartikov.replica.keyed.internal

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
import me.aartikov.replica.keyed.KeyedPhysicalReplica
import me.aartikov.replica.keyed.KeyedReplicaEvent
import me.aartikov.replica.keyed.KeyedReplicaSettings
import me.aartikov.replica.keyed.KeyedReplicaState
import me.aartikov.replica.keyed.behaviour.KeyedReplicaBehaviour
import me.aartikov.replica.keyed.internal.controllers.ChildRemovingController
import me.aartikov.replica.keyed.internal.controllers.ObserverCountController
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaObserver
import me.aartikov.replica.single.ReplicaState
import me.aartikov.replica.single.currentState

internal class KeyedPhysicalReplicaImpl<K : Any, T : Any>(
    override val coroutineScope: CoroutineScope,
    override val name: String,
    override val settings: KeyedReplicaSettings<K, T>,
    override val tags: Set<ReplicaTag>,
    behaviours: List<KeyedReplicaBehaviour<K, T>>,
    private val storageCleaner: KeyedStorageCleaner<T>?,
    private val replicaFactory: (CoroutineScope, K) -> PhysicalReplica<T>
) : KeyedPhysicalReplica<K, T> {

    override val id: ReplicaId = ReplicaId.random()

    private val _stateFlow = MutableStateFlow(KeyedReplicaState.Empty)
    override val stateFlow get() = _stateFlow.asStateFlow()

    private val _eventFlow = MutableSharedFlow<KeyedReplicaEvent<K, T>>(extraBufferCapacity = 1000)
    override val eventFlow get() = _eventFlow.asSharedFlow()

    private val replicasLock = Lock()
    private val replicas = mutableMapOf<K, PhysicalReplica<T>>()

    private val childRemovingController = ChildRemovingController<K, T>(this::removeReplica)
    private val observerCountController = ObserverCountController<T>(_stateFlow)

    init {
        behaviours.forEach { behaviour ->
            behaviour.setup(this)
        }
    }

    override fun observe(
        observerCoroutineScope: CoroutineScope,
        observerActive: StateFlow<Boolean>,
        key: StateFlow<K?>
    ): ReplicaObserver<T> {
        return KeyedReplicaObserverImpl(
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

    override suspend fun getData(key: K, forceRefresh: Boolean): T {
        return getOrCreateReplica(key).getData(forceRefresh)
    }

    override fun getCurrentState(key: K): ReplicaState<T>? {
        return getReplica(key)?.currentState
    }

    override suspend fun setData(key: K, data: T) {
        getOrCreateReplica(key).setData(data)
    }

    override suspend fun mutateData(key: K, transform: (T) -> T) {
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

    override suspend fun clear(
        key: K,
        invalidationMode: InvalidationMode,
        removeFromStorage: Boolean
    ) {
        val replica = if (storageCleaner != null && removeFromStorage) {
            getOrCreateReplica(key)
        } else {
            getReplica(key)
        }
        replica?.clear(invalidationMode, removeFromStorage)
    }

    override suspend fun clearError(key: K) {
        getReplica(key)?.clearError()
    }

    override suspend fun clearAll(invalidationMode: InvalidationMode) {
        onEachReplica {
            clear(invalidationMode, removeFromStorage = false)
        }
        storageCleaner?.removeAll()
    }

    override suspend fun beginOptimisticUpdate(key: K, update: OptimisticUpdate<T>) {
        getReplica(key)?.beginOptimisticUpdate(update)
    }

    override suspend fun commitOptimisticUpdate(key: K, update: OptimisticUpdate<T>) {
        getReplica(key)?.commitOptimisticUpdate(update)
    }

    override suspend fun rollbackOptimisticUpdate(key: K, update: OptimisticUpdate<T>) {
        getReplica(key)?.rollbackOptimisticUpdate(update)
    }

    override suspend fun onReplica(key: K, action: suspend PhysicalReplica<T>.() -> Unit) {
        getOrCreateReplica(key).apply { action() }
    }

    override suspend fun onExistingReplica(key: K, action: suspend PhysicalReplica<T>.() -> Unit) {
        getReplica(key)?.apply { action() }
    }

    override suspend fun onEachReplica(action: suspend PhysicalReplica<T>.(K) -> Unit) {
        // make a copy for concurrent modification
        val replicasCopy = replicasLock.withLock {
            ArrayList(replicas.entries)
        }
        replicasCopy.forEach { (key, replica) ->
            replica.action(key)
        }
    }

    private fun getReplica(key: K): PhysicalReplica<T>? = replicasLock.withLock {
        return replicas[key]
    }

    private fun getOrCreateReplica(key: K): PhysicalReplica<T> {
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
            _eventFlow.tryEmit(KeyedReplicaEvent.ReplicaCreated(key, replica))
        }

        return replica
    }

    private fun createReplica(key: K): PhysicalReplica<T> {
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
            _eventFlow.tryEmit(KeyedReplicaEvent.ReplicaRemoved(key, removedReplica.id))
        }
    }
}

private fun CoroutineScope.createChildScope(): CoroutineScope {
    return CoroutineScope(coroutineContext + SupervisorJob(parent = coroutineContext[Job]))
}