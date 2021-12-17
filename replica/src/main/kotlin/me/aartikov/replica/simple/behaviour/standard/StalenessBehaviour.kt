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

    private var staleJob: Job? = null

    override fun handleEvent(replica: CoreReplica<T>, event: ReplicaEvent<T>) {
        when (event) {
            is ReplicaEvent.FreshnessEvent.Freshened -> launchStaleJob(replica)
            is ReplicaEvent.FreshnessEvent.BecameStale, is ReplicaEvent.ClearedEvent -> cancelStaleJob()
            else -> Unit
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun launchStaleJob(replica: CoreReplica<T>) {
        cancelStaleJob()
        if (staleTime.isPositive()) {
            staleJob = replica.coroutineScope.launch {
                delay(staleTime)
                replica.makeStale()
            }
        } else {
            replica.makeStale()
        }
    }

    private fun cancelStaleJob() {
        staleJob?.cancel()
        staleJob = null
    }
}