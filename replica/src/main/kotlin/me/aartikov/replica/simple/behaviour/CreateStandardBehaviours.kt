package me.aartikov.replica.simple.behaviour

import me.aartikov.replica.simple.ReplicaSettings
import me.aartikov.replica.simple.behaviour.standard.ClearingBehaviour
import me.aartikov.replica.simple.behaviour.standard.RevalidateOnActivatedBehaviour
import me.aartikov.replica.simple.behaviour.standard.StalenessBehaviour

internal fun <T : Any> createStandardBehaviours(replicaSettings: ReplicaSettings) =
    buildList<ReplicaBehaviour<T>> {

        if (replicaSettings.revalidateOnActivated) {
            add(RevalidateOnActivatedBehaviour())
        }

        if (replicaSettings.staleTime != null) {
            add(StalenessBehaviour(replicaSettings.staleTime))
        }

        if (replicaSettings.clearTime != null) {
            add(ClearingBehaviour(replicaSettings.clearTime))
        }
    }