package me.aartikov.replica.decompose.internal

import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

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