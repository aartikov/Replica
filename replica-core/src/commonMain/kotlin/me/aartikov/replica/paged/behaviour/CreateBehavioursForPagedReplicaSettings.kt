package me.aartikov.replica.paged.behaviour

import me.aartikov.replica.common.ObservingStatus
import me.aartikov.replica.network.NetworkConnectivityProvider
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedLoadingStatus
import me.aartikov.replica.paged.PagedReplicaEvent
import me.aartikov.replica.paged.PagedReplicaSettings
import me.aartikov.replica.paged.PhysicalPagedReplica
import me.aartikov.replica.paged.behaviour.standard.DoOnEvent
import me.aartikov.replica.paged.behaviour.standard.DoOnNetworkConnectivityChanged
import me.aartikov.replica.paged.behaviour.standard.DoOnStateCondition
import me.aartikov.replica.paged.behaviour.standard.StaleAfterGivenTime
import me.aartikov.replica.paged.currentState
import kotlin.time.Duration

internal fun <T : Any, P : Page<T>> createBehavioursForPagedReplicaSettings(
    settings: PagedReplicaSettings,
    networkConnectivityProvider: NetworkConnectivityProvider?
) = buildList<PagedReplicaBehaviour<T, P>> {

    settings.staleTime?.let {
        add(StaleAfterGivenTime(it))
    }

    settings.clearTime?.let {
        add(createClearingBehaviour(it))
    }

    settings.clearErrorTime?.let {
        add(createErrorClearingBehaviour(it))
    }

    settings.cancelTime?.let {
        add(createCancellationBehaviour(it))
    }

    if (settings.revalidateOnActiveObserverAdded) {
        add(createRevalidationOnActiveObserverAddedBehaviour())
    }

    if (networkConnectivityProvider != null && settings.revalidateOnNetworkConnection) {
        add(createRevalidationOnNetworkConnectionBehaviour(networkConnectivityProvider))
    }
}

private fun <T : Any, P : Page<T>> createClearingBehaviour(clearTime: Duration): PagedReplicaBehaviour<T, P> {
    return DoOnStateCondition(
        condition = {
            (it.data != null || it.error != null) && it.loading == PagedLoadingStatus.None
                    && it.observingState.status == ObservingStatus.None
        },
        startDelay = clearTime,
        action = PhysicalPagedReplica<T, P>::clear
    )
}

private fun <T : Any, P : Page<T>> createErrorClearingBehaviour(clearErrorTime: Duration): PagedReplicaBehaviour<T, P> {
    return DoOnStateCondition(
        condition = {
            it.error != null && it.loading == PagedLoadingStatus.None && it.observingState.status == ObservingStatus.None
        },
        startDelay = clearErrorTime,
        action = PhysicalPagedReplica<T, P>::clearError
    )
}

private fun <T : Any, P : Page<T>> createCancellationBehaviour(cancelTime: Duration): PagedReplicaBehaviour<T, P> {
    return DoOnStateCondition(
        condition = {
            it.loading != PagedLoadingStatus.None && !it.preloading
                    && it.observingState.status == ObservingStatus.None
        },
        startDelay = cancelTime,
        action = PhysicalPagedReplica<T, P>::cancel
    )
}

private fun <T : Any, P : Page<T>> createRevalidationOnActiveObserverAddedBehaviour(): PagedReplicaBehaviour<T, P> {
    return DoOnEvent { event ->
        if (event is PagedReplicaEvent.ObserverCountChangedEvent
            && event.activeCount > event.previousActiveCount
        ) {
            revalidate()
        }
    }
}

private fun <T : Any, P : Page<T>> createRevalidationOnNetworkConnectionBehaviour(
    networkConnectivityProvider: NetworkConnectivityProvider
): PagedReplicaBehaviour<T, P> {
    return DoOnNetworkConnectivityChanged(networkConnectivityProvider) { connected ->
        if (connected && currentState.observingState.status == ObservingStatus.Active) {
            revalidate()
        }
    }
}