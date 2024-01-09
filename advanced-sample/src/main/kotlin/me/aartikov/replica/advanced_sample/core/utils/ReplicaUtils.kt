package me.aartikov.replica.advanced_sample.core.utils

import com.arkivanov.essenty.lifecycle.Lifecycle
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.advanced_sample.core.error_handling.ErrorHandler
import me.aartikov.replica.decompose.coroutineScope
import me.aartikov.replica.decompose.observe
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.Paged
import me.aartikov.replica.paged.PagedReplica
import me.aartikov.replica.paged.currentState
import me.aartikov.replica.single.Loadable
import me.aartikov.replica.single.Replica
import me.aartikov.replica.single.currentState

/**
 * Observes [Replica] and handles errors by [ErrorHandler].
 * @return Replica [Loadable] state as StateFlow
 */
fun <T : Any> Replica<T>.observe(
    lifecycle: Lifecycle,
    errorHandler: ErrorHandler
): StateFlow<Loadable<T>> {

    val coroutineScope = lifecycle.coroutineScope()
    val observer = observe(lifecycle)

    observer
        .loadingErrorFlow
        .onEach { error ->
            errorHandler.handleError(
                error.exception,
                showError = observer.currentState.data != null  // show error only if fullscreen error is not shown
            )
        }
        .launchIn(coroutineScope)

    return observer.stateFlow
}

/**
 * Observes [PagedReplica] and handles errors by [ErrorHandler].
 * @return Replica [Paged] state as StateFlow
 */
fun <T : Any, P : Page<T>> PagedReplica<T, P>.observe(
    lifecycle: Lifecycle,
    errorHandler: ErrorHandler
): StateFlow<Paged<T, P>> {

    val coroutineScope = lifecycle.coroutineScope()
    val observer = observe(lifecycle)

    observer
        .loadingErrorFlow
        .onEach { error ->
            errorHandler.handleError(
                error.exception,
                showError = observer.currentState.data != null  // show error only if fullscreen error is not shown
            )
        }
        .launchIn(coroutineScope)

    return observer.stateFlow
}