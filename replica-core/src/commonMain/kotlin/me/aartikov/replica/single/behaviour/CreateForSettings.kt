package me.aartikov.replica.single.behaviour

import me.aartikov.replica.common.ObservingStatus
import me.aartikov.replica.single.*
import me.aartikov.replica.single.behaviour.standard.*
import kotlin.time.Duration

internal fun <T : Any> ReplicaBehaviour.Companion.createForSettings(
    settings: ReplicaSettings
) = buildList<ReplicaBehaviour<T>> {

    settings.staleTime?.let {
        add(ReplicaBehaviour.staleAfterGivenTime(it))
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

    if (settings.revalidateOnNetworkConnection) {
        add(createRevalidationOnNetworkConnectionBehaviour())
    }
}

private fun <T : Any> createClearingBehaviour(clearTime: Duration): ReplicaBehaviour<T> {
    return ReplicaBehaviour.doOnStateCondition(
        condition = {
            (it.data != null || it.error != null) && !it.loading
                && it.observingState.status == ObservingStatus.None
        },
        startDelay = clearTime,
        action = PhysicalReplica<T>::clear
    )
}

private fun <T : Any> createErrorClearingBehaviour(clearErrorTime: Duration): ReplicaBehaviour<T> {
    return ReplicaBehaviour.doOnStateCondition(
        condition = {
            it.error != null && !it.loading && it.observingState.status == ObservingStatus.None
        },
        startDelay = clearErrorTime,
        action = PhysicalReplica<T>::clearError
    )
}

private fun <T : Any> createCancellationBehaviour(cancelTime: Duration): ReplicaBehaviour<T> {
    return ReplicaBehaviour.doOnStateCondition(
        condition = {
            it.loading && !it.dataRequested && !it.preloading
                && it.observingState.status == ObservingStatus.None
        },
        startDelay = cancelTime,
        action = PhysicalReplica<T>::cancel
    )
}

private fun <T : Any> createRevalidationOnActiveObserverAddedBehaviour(): ReplicaBehaviour<T> {
    return ReplicaBehaviour.doOnEvent { event ->
        if (event is ReplicaEvent.ObserverCountChangedEvent
            && event.activeCount > event.previousActiveCount
        ) {
            revalidate()
        }
    }
}

private fun <T : Any> createRevalidationOnNetworkConnectionBehaviour(): ReplicaBehaviour<T> {
    return ReplicaBehaviour.doOnNetworkConnectivityChanged { connected ->
        if (connected && currentState.observingState.status == ObservingStatus.Active) {
            revalidate()
        }
    }
}