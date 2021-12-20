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
import timber.log.Timber

fun <T : Any, K : Any> KeyedReplica<K, T>.observe(
    lifecycle: Lifecycle,
    errorHandler: ErrorHandler,
    key: () -> K,
    keepPreviousData: Boolean = false
): State<Loadable<T>> {
    val coroutineScope = lifecycle.coroutineScope()
    val keyFlow = keyFlow(coroutineScope, key)

    return observe(
        coroutineScope,
        lifecycle.activeFlow(),
        keyFlow,
        onError = { exception, state ->
            if (state.data != null) { // show error only if fullscreen error is not shown
                errorHandler.handleError(exception)
            } else {
                Timber.e(exception)
            }
        },
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