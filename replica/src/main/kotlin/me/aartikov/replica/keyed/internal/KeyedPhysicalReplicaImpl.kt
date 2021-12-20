package me.aartikov.replica.keyed.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.keyed.KeyedPhysicalReplica
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaObserver
import me.aartikov.replica.single.ReplicaState
import me.aartikov.replica.single.state
import java.util.concurrent.ConcurrentHashMap

internal class KeyedPhysicalReplicaImpl<K : Any, T : Any>(
    override val coroutineScope: CoroutineScope,
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

    override fun getState(key: K): ReplicaState<T>? {
        return getReplica(key)?.state
    }

    override fun setData(key: K, data: T) {
        getOrCreateReplica(key).setData(data)
    }

    override fun mutateData(key: K, transform: (T) -> T) {
        getReplica(key)?.mutateData(transform)
    }

    override fun makeFresh(key: K) {
        getReplica(key)?.makeFresh()
    }

    override fun makeStale(key: K) {
        getReplica(key)?.makeStale()
    }

    override fun cancelLoading(key: K) {
        getReplica(key)?.cancelLoading()
    }

    override fun clear(key: K) {
        getReplica(key)?.clear()
    }

    override fun clearError(key: K) {
        getReplica(key)?.clearError()
    }

    override fun onReplica(key: K, action: PhysicalReplica<T>.() -> Unit) {
        getOrCreateReplica(key).apply(action)
    }

    override fun onExistingReplica(key: K, action: PhysicalReplica<T>.() -> Unit) {
        getReplica(key)?.apply(action)
    }

    override fun onEachReplica(action: PhysicalReplica<T>.(K) -> Unit) {
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
    get() = with(state) {
        data == null && error == null && !loading && observerCount == 0
    }

private fun CoroutineScope.createChildScope(): CoroutineScope {
    return CoroutineScope(coroutineContext + SupervisorJob(parent = coroutineContext[Job]))
}