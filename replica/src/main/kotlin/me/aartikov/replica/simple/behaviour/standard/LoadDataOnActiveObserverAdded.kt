package me.aartikov.replica.simple.behaviour.standard

import me.aartikov.replica.simple.CoreReplica
import me.aartikov.replica.simple.ReplicaEvent
import me.aartikov.replica.simple.behaviour.ReplicaBehaviour

internal class LoadDataOnActiveObserverAdded<T : Any> : ReplicaBehaviour<T> {

    override fun handleEvent(replica: CoreReplica<T>, event: ReplicaEvent<T>) {
        if (event is ReplicaEvent.ObserverCountChanged && event.activeCount > event.previousActiveCount) {
            replica.load()
        }
    }
}