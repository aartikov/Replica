package me.aartikov.replica.single.behaviour.standard

import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaEvent
import me.aartikov.replica.single.behaviour.ReplicaBehaviour

internal class RevalidateOnActivatedBehaviour<T : Any> : ReplicaBehaviour<T> {

    override fun handleEvent(replica: PhysicalReplica<T>, event: ReplicaEvent<T>) {
        if (event is ReplicaEvent.ObserverCountChanged && event.activeCount > event.previousActiveCount) {
            replica.revalidate()
        }
    }
}