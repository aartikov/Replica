package me.aartikov.replica.single

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ReplicaObserver<out T : Any> {

    val stateFlow: StateFlow<Loadable<T>>

    val errorEventFlow: Flow<Exception>

    fun cancelObserving()
}

val <T : Any> ReplicaObserver<T>.state get() = stateFlow.value