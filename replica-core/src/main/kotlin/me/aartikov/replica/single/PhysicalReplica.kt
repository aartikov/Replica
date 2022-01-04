package me.aartikov.replica.single

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface PhysicalReplica<T : Any> : Replica<T> {

    val stateFlow: StateFlow<ReplicaState<T>>

    val eventFlow: Flow<ReplicaEvent<T>>

    fun cancelLoading()

    suspend fun setData(data: T)

    suspend fun mutateData(transform: (T) -> T)

    suspend fun invalidate(refreshCondition: RefreshCondition = RefreshCondition.IfHasObservers)

    suspend fun makeFresh()

    suspend fun clear(removeFromStorage: Boolean = true) // cancels in progress loading

    suspend fun clearError()

    suspend fun beginOptimisticUpdate(update: OptimisticUpdate<T>)

    suspend fun commitOptimisticUpdate(update: OptimisticUpdate<T>)

    suspend fun rollbackOptimisticUpdate(update: OptimisticUpdate<T>)
}

val <T : Any> PhysicalReplica<T>.currentState get() = stateFlow.value