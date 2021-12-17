package me.aartikov.replica.simple

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

// TODO: подумать над названием
interface CoreReplica<T : Any> : Replica<T> {

    val coroutineScope: CoroutineScope

    val stateFlow: StateFlow<ReplicaState<T>>

    val eventFlow: Flow<ReplicaEvent<T>>

    fun setData(data: T)

    fun mutateData(transform: (T) -> T)

    fun makeFresh()

    fun makeStale()

    fun invalidate()

    fun cancelLoading()

    fun clear() // cancels in progress loading

    fun clearError()
}

val <T : Any> CoreReplica<T>.state get() = stateFlow.value