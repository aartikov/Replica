package me.aartikov.replica.keyed.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import me.aartikov.replica.single.Loadable

@OptIn(FlowPreview::class)
internal fun <T : Any> StateFlow<Loadable<T>>.keepPreviousData(
    coroutineScope: CoroutineScope
): StateFlow<Loadable<T>> {
    val result = MutableStateFlow(this.value)
    this
        .debounce { newValue ->
            if (newValue.data == null && !newValue.loading) {
                100 // wait util loading will started for a just created replica
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