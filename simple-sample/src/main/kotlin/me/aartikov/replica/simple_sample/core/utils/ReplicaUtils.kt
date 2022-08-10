package me.aartikov.replica.simple_sample.core.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.simple_sample.core.error_handling.ErrorHandler
import me.aartikov.replica.single.Loadable
import me.aartikov.replica.single.Replica
import me.aartikov.replica.single.currentState
import me.aartikov.replica.view_model.Activable
import me.aartikov.replica.view_model.observe

/**
 * Observes [Replica] and handles errors by [ErrorHandler].
 * @return Replica [Loadable] state as [StateFlow].
 */
fun <T : Any, VM> Replica<T>.observe(
    viewModel: VM,
    errorHandler: ErrorHandler
): StateFlow<Loadable<T>> where VM : ViewModel, VM : Activable {

    val observer = observe(viewModel)

    observer
        .loadingErrorFlow
        .onEach { error ->
            errorHandler.handleError(
                error.exception,
                showError = observer.currentState.data != null  // show error only if fullscreen error is not shown
            )
        }
        .launchIn(viewModel.viewModelScope)

    return observer.stateFlow
}