package me.aartikov.replica.single.behaviour.standard

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.common.InvalidationMode
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaEvent
import me.aartikov.replica.single.behaviour.ReplicaBehaviour
import kotlin.time.Duration

fun <T : Any> ReplicaBehaviour.Companion.staleAfterGivenTime(
    staleTime: Duration
): ReplicaBehaviour<T> = StaleAfterGivenTime(staleTime)

private class StaleAfterGivenTime<T : Any>(
    private val staleTime: Duration
) : ReplicaBehaviour<T> {

    private var staleJob: Job? = null

    override fun setup(replicaClient: ReplicaClient, replica: PhysicalReplica<T>) {
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