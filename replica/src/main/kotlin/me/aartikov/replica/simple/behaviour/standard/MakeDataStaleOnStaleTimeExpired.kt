package me.aartikov.replica.simple.behaviour.standard

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.aartikov.replica.simple.CoreReplica
import me.aartikov.replica.simple.ReplicaEvent
import me.aartikov.replica.simple.behaviour.ReplicaBehaviour
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

internal class MakeDataStaleOnStaleTimeExpired<T : Any>(
    private val staleTime: Duration
) : ReplicaBehaviour<T> {

    private var staleJob: Job? = null

    override fun handleEvent(replica: CoreReplica<T>, event: ReplicaEvent<T>) {
        if (event is ReplicaEvent.FreshnessEvent) {
            when (event) {
                is ReplicaEvent.FreshnessEvent.Freshened -> launchStaleJob(replica)
                is ReplicaEvent.FreshnessEvent.BecameStale -> cancelStaleJob()
            }
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