package me.aartikov.replica.keyed.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.keyed.KeyedPhysicalReplica
import me.aartikov.replica.keyed.KeyedStorage
import me.aartikov.replica.single.*
import java.util.concurrent.ConcurrentHashMap

internal class KeyedPhysicalReplicaImpl<K : Any, T : Any>(
    private val coroutineScope: CoroutineScope,
    private val storage: KeyedStorage<K, T>?,
    private val replicaFactory: (CoroutineScope, K) -> PhysicalReplica<T>
) : KeyedPhysicalReplica<K, T> {

    private val replicaElements = ConcurrentHashMap<K, KeyedReplicaElement<T>>()

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

    override suspend fun invalidate(key: K, refreshCondition: RefreshCondition) {
        if (refreshCondition == RefreshCondition.Always) {
            getOrCreateReplica(key).invalidate(refreshCondition)
        } else {
            getReplica(key)?.invalidate(refreshCondition)
        }
    }

    override suspend fun makeFresh(key: K) {
        getReplica(key)?.makeFresh()
    }

    override fun cancelLoading(key: K) {
        getReplica(key)?.cancelLoading()
    }

    override suspend fun clear(key: K, removeFromStorage: Boolean) {
        val replica = if (storage != null && removeFromStorage) {
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
        storage?.removeAll()
    }

    override suspend fun onReplica(key: K, action: suspend PhysicalReplica<T>.() -> Unit) {
        getOrCreateReplica(key).apply { action() }
    }

    override suspend fun onExistingReplica(key: K, action: suspend PhysicalReplica<T>.() -> Unit) {
        getReplica(key)?.apply { action() }
    }

    override suspend fun onEachReplica(action: suspend PhysicalReplica<T>.(K) -> Unit) {
        replicaElements.forEach { (key, element) ->
            element.replica.action(key)
        }
    }

    private fun getReplica(key: K): PhysicalReplica<T>? {
        return replicaElements[key]?.replica
    }

    private fun getOrCreateReplica(key: K): PhysicalReplica<T> {
        val element = replicaElements.computeIfAbsent(key, ::createReplicaElement)
        return element.replica
    }

    private fun createReplicaElement(key: K): KeyedReplicaElement<T> {
        val childCoroutineScope = coroutineScope.createChildScope()
        val replica = replicaFactory(childCoroutineScope, key)
        val element = KeyedReplicaElement(childCoroutineScope, replica)

        // setup auto-removing
        replica.eventFlow
            .onEach {
                if (replica.canBeRemoved) {
                    removeReplicaElement(key)
                }
            }
            .launchIn(childCoroutineScope)

        return element
    }

    private fun removeReplicaElement(key: K) {
        val removedElement = replicaElements.remove(key)
        removedElement?.coroutineScope?.cancel()
    }
}

private val <T : Any> PhysicalReplica<T>.canBeRemoved: Boolean
    get() = with(currentState) {
        data == null && error == null && !loading && observerCount == 0
    }

private fun CoroutineScope.createChildScope(): CoroutineScope {
    return CoroutineScope(coroutineContext + SupervisorJob(parent = coroutineContext[Job]))
}