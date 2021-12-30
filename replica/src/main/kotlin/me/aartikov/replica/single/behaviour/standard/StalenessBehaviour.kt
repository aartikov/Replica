package me.aartikov.replica.single.behaviour.standard

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaEvent
import me.aartikov.replica.single.behaviour.ReplicaBehaviour
import kotlin.time.Duration

internal class StalenessBehaviour<T : Any>(
    private val staleTime: Duration
) : ReplicaBehaviour<T> {

    private var staleJob: Job? = null

    override fun setup(coroutineScope: CoroutineScope, replica: PhysicalReplica<T>) {
        replica.eventFlow
            .onEach { event ->
                when (event) {
                    is ReplicaEvent.FreshnessEvent.Freshened -> {
                        coroutineScope.relaunchStaleJob(replica)
                    }
                    is ReplicaEvent.FreshnessEvent.BecameStale, is ReplicaEvent.ClearedEvent -> {
                        cancelStaleJob()
                    }
                    else -> Unit
                }
            }.launchIn(coroutineScope)
    }

    private fun CoroutineScope.relaunchStaleJob(replica: PhysicalReplica<T>) {
        staleJob?.cancel()
        staleJob = launch {
            delay(staleTime.inWholeMilliseconds)
            replica.invalidate(refreshIfHasObservers = false)
        }
    }

    private fun cancelStaleJob() {
        staleJob?.cancel()
        staleJob = null
    }
}