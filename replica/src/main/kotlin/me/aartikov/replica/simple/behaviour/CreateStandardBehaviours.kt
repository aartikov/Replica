package me.aartikov.replica.simple.behaviour

import me.aartikov.replica.simple.ReplicaSettings
import me.aartikov.replica.simple.behaviour.standard.LoadDataOnActiveObserverAdded
import me.aartikov.replica.simple.behaviour.standard.MakeStaleOnTimeExpired

internal fun <T : Any> createStandardBehaviours(replicaSettings: ReplicaSettings) =
    buildList<ReplicaBehaviour<T>> {
        if (replicaSettings.loadDataOnActiveObserverAdded) {
            add(LoadDataOnActiveObserverAdded())
        }

        if (replicaSettings.staleTime != null) {
            add(MakeStaleOnTimeExpired(replicaSettings.staleTime))
        }
    }