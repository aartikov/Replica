package me.aartikov.replica.keyed.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.aartikov.replica.single.Loadable

internal fun <T : Any> StateFlow<Loadable<T>>.keepPreviousData(
    coroutineScope: CoroutineScope
): StateFlow<Loadable<T>> {
    val result = MutableStateFlow(value)
    var previousData: T? = value.data
    var previousDataCleanupJob: Job? = null

    this
        .onEach { newValue ->
            if (newValue.data != null) {
                previousData = newValue.data
            }

            if (newValue.data == null && !newValue.loading) {
                previousDataCleanupJob?.cancel()
                previousDataCleanupJob = coroutineScope.launch {
                    delay(100)
                    previousData = null
                    previousDataCleanupJob = null
                }
            } else if (previousDataCleanupJob != null) {
                previousDataCleanupJob?.cancel()
                previousDataCleanupJob = null
            }

            result.value = if (newValue.data == null && newValue.loading) {
                newValue.copy(data = previousData)
            } else {
                newValue
            }
        }
        .launchIn(coroutineScope)

    return result
}