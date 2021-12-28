package me.aartikov.replica.single.behaviour.standard

import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.single.behaviour.ReplicaBehaviour


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

        if (replicaSettings.clearErrorTime != null) {
            add(ErrorClearingBehaviour(replicaSettings.clearErrorTime))
        }

        if (replicaSettings.cancelTime != null) {
            add(CancellationBehaviour(replicaSettings.cancelTime))
        }
    }