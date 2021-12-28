package me.aartikov.replica.keyed.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.single.Loadable

internal fun <T : Any> StateFlow<Loadable<T>>.keepPreviousData(
    coroutineScope: CoroutineScope
): StateFlow<Loadable<T>> {
    val result = MutableStateFlow(this.value)

    this.onEach { newValue ->
        result.value = if (newValue.data == null && newValue.error == null) {
            newValue.copy(data = result.value.data)
        } else {
            newValue
        }
    }.launchIn(coroutineScope)

    return result
}