package me.aartikov.replica.keyed

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.common.InvalidationMode
import me.aartikov.replica.common.OptimisticUpdate
import me.aartikov.replica.common.ReplicaId
import me.aartikov.replica.common.ReplicaTag
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaState

interface KeyedPhysicalReplica<K : Any, T : Any> : KeyedReplica<K, T> {

    val id: ReplicaId

    val name: String

    val settings: KeyedReplicaSettings<K, T>

    val coroutineScope: CoroutineScope

    val stateFlow: StateFlow<KeyedReplicaState>

    val eventFlow: Flow<KeyedReplicaEvent<K, T>>

    fun getCurrentState(key: K): ReplicaState<T>?

    suspend fun setData(key: K, data: T)

    suspend fun mutateData(key: K, transform: (T) -> T)

    suspend fun invalidate(
        key: K,
        mode: InvalidationMode = InvalidationMode.RefreshIfHasObservers
    )

    suspend fun makeFresh(key: K)

    fun cancel(key: K)

    suspend fun clear(key: K, removeFromStorage: Boolean = true) // cancels in progress loading

    suspend fun clearError(key: K)

    suspend fun clearAll()

    suspend fun beginOptimisticUpdate(key: K, update: OptimisticUpdate<T>)

    suspend fun commitOptimisticUpdate(key: K, update: OptimisticUpdate<T>)

    suspend fun rollbackOptimisticUpdate(key: K, update: OptimisticUpdate<T>)

    suspend fun onReplica(key: K, action: suspend PhysicalReplica<T>.() -> Unit)

    suspend fun onExistingReplica(key: K, action: suspend PhysicalReplica<T>.() -> Unit)

    suspend fun onEachReplica(action: suspend PhysicalReplica<T>.(K) -> Unit)
}

val <K : Any, T : Any> KeyedPhysicalReplica<K, T>.currentState get() = stateFlow.value