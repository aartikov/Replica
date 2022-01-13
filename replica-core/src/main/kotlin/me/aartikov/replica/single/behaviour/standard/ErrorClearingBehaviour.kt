package me.aartikov.replica.single.behaviour.standard

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaState
import me.aartikov.replica.single.behaviour.ReplicaBehaviour
import kotlin.time.Duration

internal class ErrorClearingBehaviour<T : Any>(
    private val clearTime: Duration
) : ReplicaBehaviour<T> {

    private var clearingJob: Job? = null

    override fun setup(replica: PhysicalReplica<T>) {
        replica.stateFlow
            .drop(1)
            .map { it.canBeCleared }
            .distinctUntilChanged()
            .onEach { canBeCleared ->
                if (canBeCleared) {
                    replica.coroutineScope.launchClearingJob(replica)
                } else {
                    cancelClearingJob()
                }
            }
            .launchIn(replica.coroutineScope)
    }

    private val ReplicaState<T>.canBeCleared: Boolean
        get() = error != null && observerCount == 0 && !loading

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