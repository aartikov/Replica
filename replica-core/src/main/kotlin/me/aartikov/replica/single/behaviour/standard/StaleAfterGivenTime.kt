package me.aartikov.replica.single.behaviour.standard

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.single.InvalidationMode
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaEvent
import me.aartikov.replica.single.behaviour.ReplicaBehaviour
import kotlin.time.Duration

internal class StaleAfterGivenTime<T : Any>(
    private val staleTime: Duration
) : ReplicaBehaviour<T> {

    private var staleJob: Job? = null

    override fun setup(replica: PhysicalReplica<T>) {
        replica.eventFlow
            .onEach { event ->
                when (event) {
                    is ReplicaEvent.FreshnessEvent.Freshened -> {
                        replica.coroutineScope.relaunchStaleJob(replica)
                    }
                    is ReplicaEvent.FreshnessEvent.BecameStale, is ReplicaEvent.ClearedEvent -> {
                        cancelStaleJob()
                    }
                    else -> Unit
                }
            }.launchIn(replica.coroutineScope)
    }

    private fun CoroutineScope.relaunchStaleJob(replica: PhysicalReplica<T>) {
        staleJob?.cancel()
        staleJob = launch {
            delay(staleTime.inWholeMilliseconds)
            withContext(NonCancellable) {
                replica.invalidate(InvalidationMode.DontRefresh)
            }
        }
    }

    private fun cancelStaleJob() {
        staleJob?.cancel()
        staleJob = null
    }
}