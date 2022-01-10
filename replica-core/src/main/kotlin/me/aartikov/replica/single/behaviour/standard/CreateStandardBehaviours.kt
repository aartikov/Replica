package me.aartikov.replica.single.behaviour.standard

import me.aartikov.replica.network.NetworkConnectivityProvider
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.single.RevalidateAction
import me.aartikov.replica.single.behaviour.ReplicaBehaviour


internal fun <T : Any> createStandardBehaviours(
    replicaSettings: ReplicaSettings,
    networkConnectivityProvider: NetworkConnectivityProvider?
) =
    buildList<ReplicaBehaviour<T>> {

        if (replicaSettings.revalidateOnActivated) {
            add(RevalidationOnActivatedBehaviour())
        }

        if (replicaSettings.staleTime != null) {
            add(
                StalenessBehaviour(replicaSettings.staleTime, replicaSettings.refreshOnStale)
            )
        }

        if (networkConnectivityProvider != null
            && replicaSettings.revalidateOnNetworkConnection != RevalidateAction.DontRevalidate
        ) {
            add(
                RevalidationOnNetworkConnectionBehaviour(
                    networkConnectivityProvider,
                    replicaSettings.revalidateOnNetworkConnection
                )
            )
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