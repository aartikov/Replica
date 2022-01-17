package me.aartikov.replica.single.behaviour.standard

import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.network.NetworkConnectivityProvider
import me.aartikov.replica.single.*
import me.aartikov.replica.single.behaviour.ReplicaBehaviour

internal class RevalidationOnNetworkConnectionBehaviour<T : Any>(
    private val networkConnectivityProvider: NetworkConnectivityProvider,
    private val revalidateOnNetworkConnection: RevalidateAction
) : ReplicaBehaviour<T> {

    override fun setup(replica: PhysicalReplica<T>) {
        networkConnectivityProvider.connected
            .drop(1)
            .onEach { connected ->
                if (connected && replica.currentState.shouldRevalidate) {
                    replica.revalidate()
                }
            }
            .launchIn(replica.coroutineScope)
    }

    private val ReplicaState<T>.shouldRevalidate: Boolean
        get() = when (revalidateOnNetworkConnection) {
            RevalidateAction.Revalidate -> true
            RevalidateAction.RevalidateIfHasObservers -> observingStatus != ObservingStatus.None
            RevalidateAction.RevalidateIfHasActiveObservers -> observingStatus == ObservingStatus.Active
            RevalidateAction.DontRevalidate -> false
        }
}