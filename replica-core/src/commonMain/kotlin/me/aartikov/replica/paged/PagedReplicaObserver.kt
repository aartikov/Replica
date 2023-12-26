package me.aartikov.replica.paged

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.common.LoadingError

interface PagedReplicaObserver<out T : Any, out P : Page<T>> {

    val stateFlow: StateFlow<Paged<T, P>>

    val loadingErrorFlow: Flow<LoadingError>

    fun cancelObserving()
}

val <T : Any, P : Page<T>> PagedReplicaObserver<T, P>.currentState get() = stateFlow.value