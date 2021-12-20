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

internal class CancellationBehaviour<T : Any>(
    private val cancelTime: Duration
) : ReplicaBehaviour<T> {

    private var job: Job? = null

    override fun handleEvent(replica: PhysicalReplica<T>, event: ReplicaEvent<T>) {
        when (event) {
            is ReplicaEvent.ObserverCountChanged, is ReplicaEvent.LoadingEvent -> {
                if (replica.state.canBeCanceled) {
                    launchJob(replica)
                } else {
                    cancelJob()
                }
            }
            else -> Unit
        }
    }

    private val ReplicaState<T>.canBeCanceled: Boolean get() = observerCount == 0 && loading && !dataRequested

    private fun launchJob(replica: PhysicalReplica<T>) {
        cancelJob()
        if (cancelTime.isPositive()) {
            job = replica.coroutineScope.launch {
                delay(cancelTime.inWholeMilliseconds)
                replica.cancelLoading()
            }
        } else {
            replica.cancelLoading()
        }
    }

    private fun cancelJob() {
        job?.cancel()
        job = null
    }
}