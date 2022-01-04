package me.aartikov.replica.sample.core.ui.utils

import androidx.compose.runtime.State
import com.arkivanov.essenty.lifecycle.Lifecycle
import me.aartikov.replica.decompose.observe
import me.aartikov.replica.sample.core.ui.error_handing.ErrorHandler
import me.aartikov.replica.single.Loadable
import me.aartikov.replica.single.Replica
import timber.log.Timber

fun <T : Any> Replica<T>.observe(
    lifecycle: Lifecycle,
    errorHandler: ErrorHandler
): State<Loadable<T>> {
    return observe(
        lifecycle,
        onError = { error, state ->
            if (state.data != null) { // show error only if fullscreen error is not shown
                errorHandler.handleError(error.exception)
            } else {
                Timber.e(error.exception)
            }
        }
    )
}