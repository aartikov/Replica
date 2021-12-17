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

internal class ClearingBehaviour<T : Any>(
    private val clearTime: Duration
) : ReplicaBehaviour<T> {

    private var clearJob: Job? = null

    override fun handleEvent(replica: CoreReplica<T>, event: ReplicaEvent<T>) {
        when (event) {
            is ReplicaEvent.ObserverCountChanged, is ReplicaEvent.LoadingEvent -> {
                if (replica.state.isUsed) {
                    cancelClearJob()
                } else {
                    launchClearJob(replica)
                }
            }
            else -> Unit
        }
    }

    private val ReplicaState<T>.isUsed: Boolean get() = observerCount > 0 || loading

    @OptIn(ExperimentalTime::class)
    private fun launchClearJob(replica: CoreReplica<T>) {
        cancelClearJob()
        if (clearTime.isPositive()) {
            clearJob = replica.coroutineScope.launch {
                delay(clearTime)
                replica.clear()
            }
        } else {
            replica.clear()
        }
    }

    private fun cancelClearJob() {
        clearJob?.cancel()
        clearJob = null
    }
}