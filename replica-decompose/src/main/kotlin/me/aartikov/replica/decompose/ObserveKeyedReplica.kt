package me.aartikov.replica.decompose

import androidx.compose.runtime.State
import com.arkivanov.essenty.lifecycle.Lifecycle
import me.aartikov.replica.common.LoadingError
import me.aartikov.replica.keyed.KeyedReplica
import me.aartikov.replica.keyed.observe
import me.aartikov.replica.single.Loadable

fun <T : Any, K : Any> KeyedReplica<K, T>.observe(
    lifecycle: Lifecycle,
    onError: (LoadingError, Loadable<T>) -> Unit,
    key: () -> K?,
    keepPreviousData: Boolean = false
): State<Loadable<T>> {
    val coroutineScope = lifecycle.coroutineScope()
    val keyFlow = snapshotStateFlow(coroutineScope, key)

    return this.observe(
        coroutineScope,
        lifecycle.activeFlow(),
        keyFlow,
        onError,
        keepPreviousData
    ).toComposeState(coroutineScope)
}