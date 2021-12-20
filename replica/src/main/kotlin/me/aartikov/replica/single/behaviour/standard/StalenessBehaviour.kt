package me.aartikov.replica.single.behaviour.standard

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaEvent
import me.aartikov.replica.single.behaviour.ReplicaBehaviour
import kotlin.time.Duration

internal class StalenessBehaviour<T : Any>(
    private val staleTime: Duration
) : ReplicaBehaviour<T> {

    private var job: Job? = null

    override fun handleEvent(replica: PhysicalReplica<T>, event: ReplicaEvent<T>) {
        when (event) {
            is ReplicaEvent.FreshnessEvent.Freshened -> launchJob(replica)
            is ReplicaEvent.FreshnessEvent.BecameStale, is ReplicaEvent.ClearedEvent -> cancelJob()
            else -> Unit
        }
    }

    private fun launchJob(replica: PhysicalReplica<T>) {
        cancelJob()
        if (staleTime.isPositive()) {
            job = replica.coroutineScope.launch {
                delay(staleTime.inWholeMilliseconds)
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