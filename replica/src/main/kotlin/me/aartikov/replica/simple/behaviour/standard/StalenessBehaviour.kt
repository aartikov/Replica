package me.aartikov.replica.simple.behaviour.standard

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.aartikov.replica.simple.CoreReplica
import me.aartikov.replica.simple.ReplicaEvent
import me.aartikov.replica.simple.behaviour.ReplicaBehaviour
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

internal class StalenessBehaviour<T : Any>(
    private val staleTime: Duration
) : ReplicaBehaviour<T> {

    private var job: Job? = null

    override fun handleEvent(replica: CoreReplica<T>, event: ReplicaEvent<T>) {
        when (event) {
            is ReplicaEvent.FreshnessEvent.Freshened -> launchJob(replica)
            is ReplicaEvent.FreshnessEvent.BecameStale, is ReplicaEvent.ClearedEvent -> cancelJob()
            else -> Unit
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun launchJob(replica: CoreReplica<T>) {
        cancelJob()
        if (staleTime.isPositive()) {
            job = replica.coroutineScope.launch {
                delay(staleTime)
                replica.makeStale()
            }
        } else {
            replica.makeStale()
        }
    }

    private fun cancelJob() {
        job?.cancel()
        job = null
    }
}