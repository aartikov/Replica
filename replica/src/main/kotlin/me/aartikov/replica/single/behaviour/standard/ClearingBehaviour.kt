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

internal class ClearingBehaviour<T : Any>(
    private val clearTime: Duration
) : ReplicaBehaviour<T> {

    private var job: Job? = null

    override fun handleEvent(replica: PhysicalReplica<T>, event: ReplicaEvent<T>) {
        when (event) {
            is ReplicaEvent.ObserverCountChanged,
            is ReplicaEvent.LoadingEvent,
            is ReplicaEvent.DataEvent -> {
                if (replica.state.canBeCleared) {
                    launchJob(replica)
                } else {
                    cancelJob()
                }
            }
            else -> Unit
        }
    }

    private val ReplicaState<T>.canBeCleared: Boolean get() = observerCount == 0 && !loading

    private fun launchJob(replica: PhysicalReplica<T>) {
        cancelJob()
        if (clearTime.isPositive()) {
            job = replica.coroutineScope.launch {
                delay(clearTime.inWholeMilliseconds)
                replica.clear()
            }
        } else {
            replica.clear()
        }
    }

    private fun cancelJob() {
        job?.cancel()
        job = null
    }
}