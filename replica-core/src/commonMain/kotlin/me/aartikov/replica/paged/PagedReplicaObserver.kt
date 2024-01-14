package me.aartikov.replica.paged

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.common.LoadingError

interface PagedReplicaObserver<out T : Any> {

    val stateFlow: StateFlow<Paged<T>>

    val loadingErrorFlow: Flow<LoadingError>

    fun cancelObserving()
}

val <T : Any> PagedReplicaObserver<T>.currentState get() = stateFlow.value