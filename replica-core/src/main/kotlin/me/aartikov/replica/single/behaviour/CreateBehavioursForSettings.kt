package me.aartikov.replica.single.behaviour

import me.aartikov.replica.network.NetworkConnectivityProvider
import me.aartikov.replica.single.*
import me.aartikov.replica.single.behaviour.standard.*
import kotlin.time.Duration


internal fun <T : Any> createBehavioursForSettings(
    replicaSettings: ReplicaSettings,
    networkConnectivityProvider: NetworkConnectivityProvider?
) = buildList<ReplicaBehaviour<T>> {

    replicaSettings.staleTime?.let {
        add(StaleAfterGivenTime(it))
    }

    replicaSettings.clearTime?.let {
        add(createClearingBehaviour(it))
    }

    replicaSettings.clearErrorTime?.let {
        add(createErrorClearingBehaviour(it))
    }

    replicaSettings.cancelTime?.let {
        add(createCancellationBehaviour(it))
    }

    if (replicaSettings.revalidateOnActiveObserverAdded) {
        add(createRevalidationOnActiveObserverAddedBehaviour())
    }

    if (networkConnectivityProvider != null && replicaSettings.revalidateOnNetworkConnection) {
        add(createRevalidationOnNetworkConnectionBehaviour(networkConnectivityProvider))
    }
}

private fun <T : Any> createClearingBehaviour(clearTime: Duration): ReplicaBehaviour<T> {
    return DoOnStateCondition(
        condition = {
            (it.data != null || it.error != null) && !it.loading
                && it.observingStatus == ObservingStatus.None
        },
        startDelay = clearTime,
        action = PhysicalReplica<T>::clear
    )
}

private fun <T : Any> createErrorClearingBehaviour(clearErrorTime: Duration): ReplicaBehaviour<T> {
    return DoOnStateCondition(
        condition = {
            it.error != null && !it.loading && it.observingStatus == ObservingStatus.None
        },
        startDelay = clearErrorTime,
        action = PhysicalReplica<T>::clearError
    )
}

private fun <T : Any> createCancellationBehaviour(cancelTime: Duration): ReplicaBehaviour<T> {
    return DoOnStateCondition(
        condition = {
            it.loading && !it.dataRequested && !it.preloading
                && it.observingStatus == ObservingStatus.None
        },
        startDelay = cancelTime,
        action = PhysicalReplica<T>::cancel
    )
}

private fun <T : Any> createRevalidationOnActiveObserverAddedBehaviour(): ReplicaBehaviour<T> {
    return DoOnEvent { event ->
        if (event is ReplicaEvent.ObserverCountChangedEvent
            && event.activeCount > event.previousActiveCount
        ) {
            revalidate()
        }
    }
}

private fun <T : Any> createRevalidationOnNetworkConnectionBehaviour(
    networkConnectivityProvider: NetworkConnectivityProvider
): ReplicaBehaviour<T> {
    return DoOnNetworkConnectivityChanged(networkConnectivityProvider) { connected ->
        if (connected && currentState.observingStatus == ObservingStatus.Active) {
            revalidate()
        }
    }
}