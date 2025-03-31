package me.aartikov.replica.paged.behaviour

import me.aartikov.replica.common.ObservingStatus
import me.aartikov.replica.network.NetworkConnectivityProvider
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedLoadingStatus
import me.aartikov.replica.paged.PagedPhysicalReplica
import me.aartikov.replica.paged.PagedReplicaEvent
import me.aartikov.replica.paged.PagedReplicaSettings
import me.aartikov.replica.paged.behaviour.standard.doOnEvent
import me.aartikov.replica.paged.behaviour.standard.doOnNetworkConnectivityChanged
import me.aartikov.replica.paged.behaviour.standard.doOnStateCondition
import me.aartikov.replica.paged.behaviour.standard.staleAfterGivenTime
import me.aartikov.replica.paged.currentState
import kotlin.time.Duration

internal fun <I : Any, P : Page<I>> PagedReplicaBehaviour.Companion.createForSettings(
    settings: PagedReplicaSettings,
    networkConnectivityProvider: NetworkConnectivityProvider?
) = buildList<PagedReplicaBehaviour<I, P>> {

    settings.staleTime?.let {
        add(PagedReplicaBehaviour.staleAfterGivenTime(it))
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

private fun <I : Any, P : Page<I>> createClearingBehaviour(clearTime: Duration): PagedReplicaBehaviour<I, P> {
    return PagedReplicaBehaviour.doOnStateCondition(
        condition = {
            (it.data != null || it.error != null) && it.loadingStatus == PagedLoadingStatus.None
                        && it.observingState.status == ObservingStatus.None
        },
        startDelay = clearTime,
        action = PagedPhysicalReplica<I, P>::clear
    )
}

private fun <I : Any, P : Page<I>> createErrorClearingBehaviour(clearErrorTime: Duration): PagedReplicaBehaviour<I, P> {
    return PagedReplicaBehaviour.doOnStateCondition(
        condition = {
            it.error != null && it.loadingStatus == PagedLoadingStatus.None && it.observingState.status == ObservingStatus.None
        },
        startDelay = clearErrorTime,
        action = PagedPhysicalReplica<I, P>::clearError
    )
}

private fun <I : Any, P : Page<I>> createCancellationBehaviour(cancelTime: Duration): PagedReplicaBehaviour<I, P> {
    return PagedReplicaBehaviour.doOnStateCondition(
        condition = {
            it.loadingStatus != PagedLoadingStatus.None && !it.preloading
                        && it.observingState.status == ObservingStatus.None
        },
        startDelay = cancelTime,
        action = PagedPhysicalReplica<I, P>::cancel
    )
}

private fun <I : Any, P : Page<I>> createRevalidationOnActiveObserverAddedBehaviour(): PagedReplicaBehaviour<I, P> {
    return PagedReplicaBehaviour.doOnEvent { event ->
        if (event is PagedReplicaEvent.ObserverCountChangedEvent
            && event.activeCount > event.previousActiveCount
        ) {
            revalidate()
        }
    }
}

private fun <I : Any, P : Page<I>> createRevalidationOnNetworkConnectionBehaviour(
    networkConnectivityProvider: NetworkConnectivityProvider
): PagedReplicaBehaviour<I, P> {
    return PagedReplicaBehaviour.doOnNetworkConnectivityChanged(networkConnectivityProvider) { connected ->
        if (connected && currentState.observingState.status == ObservingStatus.Active) {
            revalidate()
        }
    }
}