package me.aartikov.replica.single.behaviour.standard

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaEvent
import me.aartikov.replica.single.ReplicaState
import me.aartikov.replica.single.behaviour.ReplicaBehaviour
import me.aartikov.replica.single.currentState
import kotlin.time.Duration

internal class ClearingBehaviour<T : Any>(
    private val clearTime: Duration
) : ReplicaBehaviour<T> {

    private var clearingJob: Job? = null

    override fun setup(coroutineScope: CoroutineScope, replica: PhysicalReplica<T>) {
        replica.stateFlow
            .map { it.data != null }
            .distinctUntilChanged()
            .onEach {
                if (replica.currentState.canBeCleared) {
                    coroutineScope.launchClearingJob(replica)
                } else {
                    cancelClearingJob()
                }
            }
            .launchIn(coroutineScope)

        replica.eventFlow
            .onEach { event ->
                when (event) {
                    is ReplicaEvent.ObserverCountChangedEvent,
                    is ReplicaEvent.LoadingEvent.LoadingStarted,
                    is ReplicaEvent.LoadingEvent.LoadingFinished -> {
                        if (replica.currentState.canBeCleared) {
                            coroutineScope.launchClearingJob(replica)
                        } else {
                            cancelClearingJob()
                        }
                    }
                    else -> Unit
                }
            }
            .launchIn(coroutineScope)
    }

    private val ReplicaState<T>.canBeCleared: Boolean get() = observerCount == 0 && !loading

    private fun CoroutineScope.launchClearingJob(replica: PhysicalReplica<T>) {
        if (clearingJob?.isActive == true) return

        clearingJob = launch {
            delay(clearTime.inWholeMilliseconds)
            replica.clear()
        }
    }

    private fun cancelClearingJob() {
        clearingJob?.cancel()
        clearingJob = null
    }
}