package me.aartikov.replica.keyed.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import me.aartikov.replica.keyed.KeyedPhysicalReplica
import me.aartikov.replica.keyed.KeyedReplicaEvent
import me.aartikov.replica.keyed.KeyedReplicaId
import me.aartikov.replica.single.*
import java.util.concurrent.ConcurrentHashMap

internal class KeyedPhysicalReplicaImpl<K : Any, T : Any>(
    override val coroutineScope: CoroutineScope,
    override val name: String,
    private val storageCleaner: KeyedStorageCleaner<T>?,
    private val replicaFactory: (CoroutineScope, K) -> PhysicalReplica<T>
) : KeyedPhysicalReplica<K, T> {

    override val id: KeyedReplicaId = KeyedReplicaId.random()

    private val _eventFlow = MutableSharedFlow<KeyedReplicaEvent<K, T>>(extraBufferCapacity = 1000)
    override val eventFlow get() = _eventFlow.asSharedFlow()

    private val replicas = ConcurrentHashMap<K, PhysicalReplica<T>>()

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

    override suspend fun getData(key: K): T {
        return getOrCreateReplica(key).getData()
    }

    override suspend fun getRefreshedData(key: K): T {
        return getOrCreateReplica(key).getRefreshedData()
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

    override suspend fun clear(key: K, removeFromStorage: Boolean) {
        val replica = if (storageCleaner != null && removeFromStorage) {
            getOrCreateReplica(key)
        } else {
            getReplica(key)
        }
        replica?.clear(removeFromStorage)
    }

    override suspend fun clearError(key: K) {
        getReplica(key)?.clearError()
    }

    override suspend fun clearAll() {
        onEachReplica {
            clear(removeFromStorage = false)
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
        replicas.forEach { (key, replica) ->
            replica.action(key)
        }
    }

    private fun getReplica(key: K): PhysicalReplica<T>? {
        return replicas[key]
    }

    private fun getOrCreateReplica(key: K): PhysicalReplica<T> {
        var created = false
        val replica = replicas.computeIfAbsent(key) {
            createReplica(key).also {
                created = true
            }
        }

        if (created) {
            _eventFlow.tryEmit(KeyedReplicaEvent.ReplicaCreated(key, replica))
        }

        return replica
    }

    private fun createReplica(key: K): PhysicalReplica<T> {
        val childCoroutineScope = coroutineScope.createChildScope()
        val replica = replicaFactory(childCoroutineScope, key)

        // setup auto-removing
        replica.stateFlow
            .drop(1)
            .onEach { state ->
                if (state.canBeRemoved) {
                    removeReplicaElement(key)
                }
            }
            .launchIn(childCoroutineScope)

        return replica
    }

    private fun removeReplicaElement(key: K) {
        val removedReplica = replicas.remove(key)
        if (removedReplica != null) {
            removedReplica.coroutineScope.cancel()
            _eventFlow.tryEmit(KeyedReplicaEvent.ReplicaRemoved(key, removedReplica.id))
        }
    }
}

private val <T : Any> ReplicaState<T>.canBeRemoved: Boolean
    get() = data == null && error == null && !loading && observingStatus == ObservingStatus.None


private fun CoroutineScope.createChildScope(): CoroutineScope {
    return CoroutineScope(coroutineContext + SupervisorJob(parent = coroutineContext[Job]))
}