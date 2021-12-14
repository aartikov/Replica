package me.aartikov.replica.lifecycle

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal fun <T> Flow<T>.toActivableFlow(
    coroutineScope: CoroutineScope,
    activeFlow: Flow<Boolean>
): Flow<T> {
    val originalFlow: Flow<T> = this
    val resultFlow = MutableSharedFlow<T>()
    var job: Job? = null

    activeFlow
        .onEach { active ->
            if (active) {
                job = originalFlow
                    .onEach { resultFlow.emit(it) }
                    .launchIn(coroutineScope)
            } else {
                job?.cancel()
                job = null
            }
        }
        .launchIn(coroutineScope)

    return resultFlow
}