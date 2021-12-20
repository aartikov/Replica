package me.aartikov.replica.sample.core.ui.utils

import androidx.compose.runtime.State
import com.arkivanov.essenty.lifecycle.Lifecycle
import me.aartikov.replica.sample.core.ui.error_handing.ErrorHandler
import me.aartikov.replica.single.Loadable
import me.aartikov.replica.single.Replica
import me.aartikov.replica.single.observe
import timber.log.Timber

fun <T : Any> Replica<T>.observe(
    lifecycle: Lifecycle,
    errorHandler: ErrorHandler
): State<Loadable<T>> {
    val coroutineScope = lifecycle.coroutineScope()
    return this.observe(
        coroutineScope,
        lifecycle.activeFlow(),
        onError = { exception, state ->
            if (state.data != null) { // show error only if fullscreen error is not shown
                errorHandler.handleError(exception)
            } else {
                Timber.e(exception)
            }
        }
    ).toComposeState(coroutineScope)
}