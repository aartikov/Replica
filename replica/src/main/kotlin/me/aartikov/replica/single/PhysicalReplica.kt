package me.aartikov.replica.single

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface PhysicalReplica<T : Any> : Replica<T> {

    val coroutineScope: CoroutineScope

    val stateFlow: StateFlow<ReplicaState<T>>

    val eventFlow: Flow<ReplicaEvent<T>>

    fun setData(data: T)

    fun mutateData(transform: (T) -> T)

    fun makeFresh()

    fun makeStale()

    fun cancelLoading()

    fun clear() // cancels in progress loading

    fun clearError()

}

val <T : Any> PhysicalReplica<T>.state get() = stateFlow.value

fun <T : Any> PhysicalReplica<T>.invalidate() {
    makeStale()
    if (state.observerCount > 0) {
        refresh()
    }
}