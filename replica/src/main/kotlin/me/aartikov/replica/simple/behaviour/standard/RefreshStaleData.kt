package me.aartikov.replica.simple.behaviour.standard

import me.aartikov.replica.simple.CoreReplica
import me.aartikov.replica.simple.ReplicaEvent
import me.aartikov.replica.simple.StaleReason
import me.aartikov.replica.simple.behaviour.ReplicaBehaviour
import me.aartikov.replica.simple.state

// TODO: нужны ли настройки для reason и activeObserverCount?
// TODO: подключить в Standard Behaviours
internal class RefreshStaleData<T : Any> : ReplicaBehaviour<T> {

    override fun handleEvent(replica: CoreReplica<T>, event: ReplicaEvent<T>) {
        if (
            event is ReplicaEvent.Freshness.BecameStale
            && event.reason == StaleReason.ChangedManually
            && replica.state.activeObserverCount > 0
        ) {
            replica.refresh()
        }
    }
}