package me.aartikov.replica.simple.behaviour.standard

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.aartikov.replica.simple.CoreReplica
import me.aartikov.replica.simple.ReplicaEvent
import me.aartikov.replica.simple.ReplicaState
import me.aartikov.replica.simple.behaviour.ReplicaBehaviour
import me.aartikov.replica.simple.state
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

internal class CancellationBehaviour<T : Any>(
    private val cancelTime: Duration
) : ReplicaBehaviour<T> {

    private var job: Job? = null

    override fun handleEvent(replica: CoreReplica<T>, event: ReplicaEvent<T>) {
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

    @OptIn(ExperimentalTime::class)
    private fun launchJob(replica: CoreReplica<T>) {
        cancelJob()
        if (cancelTime.isPositive()) {
            job = replica.coroutineScope.launch {
                delay(cancelTime)
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