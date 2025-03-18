package me.aartikov.replica.simple_sample.core.utils

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.simple_sample.core.error_handling.ErrorHandler
import me.aartikov.replica.single.Loadable
import me.aartikov.replica.single.Replica
import me.aartikov.replica.single.currentState
import me.aartikov.replica.view_model.Activable
import me.aartikov.replica.view_model.replicaObserverHost

/**
 * Observes [Replica] in a scope of [ViewModel] and handles errors by [ErrorHandler]. ViewModel has to be [Activable].
 * @return Replica [Loadable] state as [StateFlow].
 */
fun <T : Any, VM> Replica<T>.observe(
    viewModel: VM,
    errorHandler: ErrorHandler
): StateFlow<Loadable<T>> where VM : ViewModel, VM : Activable {

    val observerHost = viewModel.replicaObserverHost()
    val observer = observe(observerHost)

    observer
        .loadingErrorFlow
        .onEach { error ->
            errorHandler.handleError(
                error.exception,
                showError = observer.currentState.data != null  // show error only if fullscreen error is not shown
            )
        }
        .launchIn(observerHost.observerCoroutineScope)

    return observer.stateFlow
}