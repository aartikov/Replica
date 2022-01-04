package me.aartikov.replica.decompose

import androidx.compose.runtime.State
import com.arkivanov.essenty.lifecycle.Lifecycle
import me.aartikov.replica.single.Loadable
import me.aartikov.replica.single.LoadingError
import me.aartikov.replica.single.Replica
import me.aartikov.replica.single.observe

fun <T : Any> Replica<T>.observe(
    lifecycle: Lifecycle,
    onError: (LoadingError, Loadable<T>) -> Unit
): State<Loadable<T>> {
    val coroutineScope = lifecycle.coroutineScope()
    return this.observe(
        coroutineScope,
        lifecycle.activeFlow(),
        onError
    ).toComposeState(coroutineScope)
}