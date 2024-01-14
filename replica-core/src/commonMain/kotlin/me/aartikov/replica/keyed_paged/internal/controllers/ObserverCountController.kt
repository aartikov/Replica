package me.aartikov.replica.keyed_paged.internal.controllers

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import me.aartikov.replica.keyed_paged.KeyedPagedReplicaState
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedPhysicalReplica
import me.aartikov.replica.single.ReplicaEvent

internal class ObserverCountController<I : Any, P : Page<I>>(
    private val keyedPagedReplicaStateFlow: MutableStateFlow<KeyedPagedReplicaState>
) {

    fun setupObserverCounting(replica: PagedPhysicalReplica<I, P>) {
        replica.eventFlow
            .filterIsInstance<ReplicaEvent.ObserverCountChangedEvent>()
            .onEach { event ->
                val replicaWithObserversCountDiff = when {
                    event.count > 0 && event.previousCount == 0 -> 1
                    event.count == 0 && event.previousCount > 0 -> -1
                    else -> 0
                }

                val replicaWithActiveObserversCountDiff = when {
                    event.activeCount > 0 && event.previousActiveCount == 0 -> 1
                    event.activeCount == 0 && event.previousActiveCount > 0 -> -1
                    else -> 0
                }

                if (replicaWithObserversCountDiff != 0 || replicaWithActiveObserversCountDiff != 0) {
                    keyedPagedReplicaStateFlow.update { state ->
                        state.copy(
                            replicaWithObserversCount = state.replicaWithObserversCount + replicaWithObserversCountDiff,
                            replicaWithActiveObserversCount = state.replicaWithActiveObserversCount + replicaWithActiveObserversCountDiff
                        )
                    }
                }
            }
            .launchIn(replica.coroutineScope)
    }
}