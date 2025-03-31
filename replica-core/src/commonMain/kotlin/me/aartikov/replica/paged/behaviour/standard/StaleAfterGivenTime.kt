package me.aartikov.replica.paged.behaviour.standard

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
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedPhysicalReplica
import me.aartikov.replica.paged.PagedReplicaEvent
import me.aartikov.replica.paged.behaviour.PagedReplicaBehaviour
import kotlin.time.Duration

internal fun <I : Any, P : Page<I>> PagedReplicaBehaviour.Companion.staleAfterGivenTime(
    staleTime: Duration
): PagedReplicaBehaviour<I, P> = StaleAfterGivenTime(staleTime)


private class StaleAfterGivenTime<I : Any, P : Page<I>>(
    private val staleTime: Duration
) : PagedReplicaBehaviour<I, P> {

    private var staleJob: Job? = null

    override fun setup(replicaClient: ReplicaClient, pagedReplica: PagedPhysicalReplica<I, P>) {
        pagedReplica.eventFlow
            .onEach { event ->
                when (event) {
                    is PagedReplicaEvent.FreshnessEvent.Freshened -> {
                        pagedReplica.coroutineScope.relaunchStaleJob(pagedReplica)
                    }

                    is PagedReplicaEvent.FreshnessEvent.BecameStale, is PagedReplicaEvent.ClearedEvent -> {
                        cancelStaleJob()
                    }

                    else -> Unit
                }
            }.launchIn(pagedReplica.coroutineScope)
    }

    private fun CoroutineScope.relaunchStaleJob(replica: PagedPhysicalReplica<I, P>) {
        staleJob?.cancel()
        staleJob = launch {
            delay(staleTime)
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