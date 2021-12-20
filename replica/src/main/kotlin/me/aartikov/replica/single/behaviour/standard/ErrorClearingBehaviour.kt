package me.aartikov.replica.single.behaviour.standard

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaEvent
import me.aartikov.replica.single.ReplicaState
import me.aartikov.replica.single.behaviour.ReplicaBehaviour
import me.aartikov.replica.single.state
import kotlin.time.Duration

internal class ErrorClearingBehaviour<T : Any>(
    private val clearTime: Duration
) : ReplicaBehaviour<T> {

    private var job: Job? = null

    override fun handleEvent(replica: PhysicalReplica<T>, event: ReplicaEvent<T>) {
        when (event) {
            is ReplicaEvent.ObserverCountChanged,
            is ReplicaEvent.ErrorEvent,
            is ReplicaEvent.LoadingEvent -> {
                if (replica.state.canBeCleared) {
                    launchJob(replica)
                } else {
                    cancelJob()
                }
            }
            else -> Unit
        }
    }

    private val ReplicaState<T>.canBeCleared: Boolean get() = error != null && observerCount == 0 && !loading

    private fun launchJob(replica: PhysicalReplica<T>) {
        cancelJob()
        if (clearTime.isPositive()) {
            job = replica.coroutineScope.launch {
                delay(clearTime.inWholeMilliseconds)
                replica.clearError()
            }
        } else {
            replica.clearError()
        }
    }

    private fun cancelJob() {
        job?.cancel()
        job = null
    }
}