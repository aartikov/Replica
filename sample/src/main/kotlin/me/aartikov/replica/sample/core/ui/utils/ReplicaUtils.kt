package me.aartikov.replica.sample.core.ui.utils

import androidx.compose.runtime.State
import com.arkivanov.essenty.lifecycle.Lifecycle
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.sample.core.ui.error_handing.ErrorHandler
import me.aartikov.replica.simple.Loadable
import me.aartikov.replica.simple.Replica
import me.aartikov.replica.simple.ReplicaObserver
import me.aartikov.replica.simple.state

fun <T : Any> Replica<T>.observe(lifecycle: Lifecycle): ReplicaObserver<T> {
    return observe(lifecycle.coroutineScope(), lifecycle.activeFlow())
}

fun <T : Any> Replica<T>.observeAndHandleErrors(
    lifecycle: Lifecycle,
    errorHandler: ErrorHandler
): State<Loadable<T>> {
    val coroutineScope = lifecycle.coroutineScope()
    val observer = observe(coroutineScope, lifecycle.activeFlow())
    observer
        .errorEventFlow
        .onEach { exception ->
            if (observer.state.data != null) { // show error only if fullscreen error is not shown
                errorHandler.handleError(exception)
            }
        }
        .launchIn(coroutineScope)

    return observer.stateFlow.toComposeState(coroutineScope)
}