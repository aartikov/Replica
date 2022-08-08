package me.aartikov.replica.advanced_sample.core.utils

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Converts [StateFlow] to Jetpack Compose [State]
 */
internal fun <T> StateFlow<T>.toComposeState(coroutineScope: CoroutineScope): State<T> {
    val state: MutableState<T> = mutableStateOf(this.value)
    coroutineScope.launch {
        this@toComposeState.collect {
            state.value = it
        }
    }
    return state
}

/**
 * Creates a [StateFlow] that tracks all changes of Jetpack Compose state in [block] and returns recalculated value.
 */
internal fun <T> snapshotStateFlow(
    coroutineScope: CoroutineScope,
    block: () -> T
): StateFlow<T> {
    return snapshotFlow { block() }.stateIn(
        coroutineScope,
        SharingStarted.Eagerly,
        initialValue = block()
    )
}