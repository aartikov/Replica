package me.aartikov.replica.sample.core.ui.utils

import androidx.compose.runtime.State
import com.arkivanov.essenty.lifecycle.Lifecycle
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.decompose.coroutineScope
import me.aartikov.replica.decompose.observe
import me.aartikov.replica.keyed.KeyedReplica
import me.aartikov.replica.sample.core.ui.error_handing.ErrorHandler
import me.aartikov.replica.single.Loadable
import me.aartikov.replica.single.Replica
import me.aartikov.replica.single.currentState

/**
 * Observes [Replica] and handles errors by [ErrorHandler].
 * @return Replica [Loadable] state as Jetpack Compose state
 */
fun <T : Any> Replica<T>.observe(
    lifecycle: Lifecycle,
    errorHandler: ErrorHandler
): State<Loadable<T>> {

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

    return observer.stateFlow.toComposeState(coroutineScope)
}

/**
 * Observes [KeyedReplica] and handles errors by [ErrorHandler].
 * @return Replica [Loadable] state as Jetpack Compose state
 */
fun <T : Any, K : Any> KeyedReplica<K, T>.observe(
    lifecycle: Lifecycle,
    errorHandler: ErrorHandler,
    key: () -> K?
): State<Loadable<T>> {

    val coroutineScope = lifecycle.coroutineScope()
    val observer = observe(lifecycle, snapshotStateFlow(coroutineScope, key))

    observer
        .loadingErrorFlow
        .onEach { error ->
            errorHandler.handleError(
                error.exception,
                showError = observer.currentState.data != null  // show error only if fullscreen error is not shown
            )
        }
        .launchIn(coroutineScope)

    return observer.stateFlow.toComposeState(coroutineScope)
}