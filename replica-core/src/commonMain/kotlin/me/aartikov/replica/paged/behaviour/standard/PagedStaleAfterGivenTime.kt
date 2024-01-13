package me.aartikov.replica.paged.behaviour.standard

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.aartikov.replica.common.InvalidationMode
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedPhysicalReplica
import me.aartikov.replica.paged.PagedReplicaEvent
import me.aartikov.replica.paged.behaviour.PagedReplicaBehaviour
import kotlin.time.Duration

internal class PagedStaleAfterGivenTime<T : Any, P : Page<T>>(
    private val staleTime: Duration
) : PagedReplicaBehaviour<T, P> {

    private var staleJob: Job? = null

    override fun setup(replica: PagedPhysicalReplica<T, P>) {
        replica.eventFlow
            .onEach { event ->
                when (event) {
                    is PagedReplicaEvent.FreshnessEvent.Freshened -> {
                        replica.coroutineScope.relaunchStaleJob(replica)
                    }

                    is PagedReplicaEvent.FreshnessEvent.BecameStale, is PagedReplicaEvent.ClearedEvent -> {
                        cancelStaleJob()
                    }

                    else -> Unit
                }
            }.launchIn(replica.coroutineScope)
    }

    private fun CoroutineScope.relaunchStaleJob(replica: PagedPhysicalReplica<T, P>) {
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