package me.aartikov.replica.single.behaviour.standard

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaEvent
import me.aartikov.replica.single.behaviour.ReplicaBehaviour

internal class RevalidationOnActivatedBehaviour<T : Any> : ReplicaBehaviour<T> {

    override fun setup(coroutineScope: CoroutineScope, replica: PhysicalReplica<T>) {
        replica.eventFlow
            .onEach { event ->
                if (event is ReplicaEvent.ObserverCountChangedEvent
                    && event.activeCount > event.previousActiveCount
                ) {
                    replica.revalidate()
                }
            }
            .launchIn(coroutineScope)
    }
}