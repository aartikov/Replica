package me.aartikov.replica.single

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.common.LoadingError

interface ReplicaObserver<out T : Any> {

    val stateFlow: StateFlow<Loadable<T>>

    val loadingErrorFlow: Flow<LoadingError>

    fun cancelObserving()
}

val <T : Any> ReplicaObserver<T>.currentState get() = stateFlow.value