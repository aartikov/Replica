package me.aartikov.replica.single.behaviour.standard

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.network.NetworkConnectivityProvider
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaState
import me.aartikov.replica.single.RevalidateAction
import me.aartikov.replica.single.behaviour.ReplicaBehaviour
import me.aartikov.replica.single.currentState

internal class RevalidationOnNetworkConnectionBehaviour<T : Any>(
    private val networkConnectivityProvider: NetworkConnectivityProvider,
    private val revalidateOnNetworkConnection: RevalidateAction
) : ReplicaBehaviour<T> {

    override fun setup(coroutineScope: CoroutineScope, replica: PhysicalReplica<T>) {
        networkConnectivityProvider.connected
            .drop(1)
            .onEach { connected ->
                if (connected && replica.currentState.shouldRevalidate) {
                    replica.revalidate()
                }
            }
            .launchIn(coroutineScope)
    }

    private val ReplicaState<T>.shouldRevalidate: Boolean
        get() = when (revalidateOnNetworkConnection) {
            RevalidateAction.Revalidate -> true
            RevalidateAction.RevalidateIfHasObservers -> observerCount > 0
            RevalidateAction.RevalidateIfHasActiveObservers -> activeObserverCount > 0
            RevalidateAction.DontRevalidate -> false
        }
}