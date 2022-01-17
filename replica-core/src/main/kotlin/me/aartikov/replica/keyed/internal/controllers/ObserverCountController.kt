package me.aartikov.replica.keyed.internal.controllers

import kotlinx.coroutines.flow.*
import me.aartikov.replica.keyed.KeyedReplicaState
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaEvent

internal class ObserverCountController<T : Any>(
    private val keyedReplicaStateFlow: MutableStateFlow<KeyedReplicaState>
) {

    fun setupObserverCounting(replica: PhysicalReplica<T>) {
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
                    keyedReplicaStateFlow.update { state ->
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