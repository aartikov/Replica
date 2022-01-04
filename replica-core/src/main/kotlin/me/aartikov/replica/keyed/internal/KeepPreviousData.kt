package me.aartikov.replica.keyed.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import me.aartikov.replica.single.Loadable

internal fun <T : Any> StateFlow<Loadable<T>>.keepPreviousData(
    coroutineScope: CoroutineScope
): StateFlow<Loadable<T>> {
    val result = MutableStateFlow(this.value)
    this
        .debounce { newValue -> // TODO: придумать как сделать без debounce
            if (newValue.data == null && newValue.error == null && !newValue.loading) {
                1
            } else {
                0
            }
        }
        .onEach { newValue ->
            result.value = if (newValue.data == null && newValue.loading) {
                newValue.copy(data = result.value.data)
            } else {
                newValue
            }
        }.launchIn(coroutineScope)

    return result
}