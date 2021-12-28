package me.aartikov.replica.sample.core.ui.utils

import androidx.compose.runtime.State
import androidx.compose.runtime.snapshotFlow
import com.arkivanov.essenty.lifecycle.Lifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import me.aartikov.replica.keyed.KeyedReplica
import me.aartikov.replica.keyed.observe
import me.aartikov.replica.sample.core.ui.error_handing.ErrorHandler
import me.aartikov.replica.single.Loadable
import me.aartikov.replica.single.LoadingError
import timber.log.Timber

fun <T : Any, K : Any> KeyedReplica<K, T>.observe(
    lifecycle: Lifecycle,
    errorHandler: ErrorHandler,
    key: () -> K,
    keepPreviousData: Boolean = false
): State<Loadable<T>> {
    return observe(
        lifecycle,
        onError = { error, state ->
            if (state.data != null) { // show error only if fullscreen error is not shown
                errorHandler.handleError(error.exception)
            } else {
                Timber.e(error.exception)
            }
        },
        key,
        keepPreviousData
    )
}

fun <T : Any, K : Any> KeyedReplica<K, T>.observe(
    lifecycle: Lifecycle,
    onError: (LoadingError, Loadable<T>) -> Unit,
    key: () -> K,
    keepPreviousData: Boolean = false
): State<Loadable<T>> {
    val coroutineScope = lifecycle.coroutineScope()
    val keyFlow = keyFlow(coroutineScope, key)

    return this.observe(
        coroutineScope,
        lifecycle.activeFlow(),
        keyFlow,
        onError,
        keepPreviousData
    ).toComposeState(coroutineScope)
}

private fun <K : Any> keyFlow(
    coroutineScope: CoroutineScope,
    key: () -> K
): StateFlow<K> {
    return snapshotFlow { key() }.stateIn(
        coroutineScope,
        SharingStarted.WhileSubscribed(),
        initialValue = key()
    )
}