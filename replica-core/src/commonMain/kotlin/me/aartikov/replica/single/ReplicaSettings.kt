package me.aartikov.replica.single

import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.network.NetworkConnectivityProvider
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Configures the behaviour of a replica.
 *
 * @property staleTime The duration after which fetched data becomes stale (null means never).
 * @property clearTime The duration after which data is cleared when there are no observers (null means never).
 * @property clearErrorTime The duration after which an error is cleared when there are no observers (null means never).
 * @property cancelTime The duration after which a network request is canceled when there are no observers (null means never).
 * @property revalidateOnActiveObserverAdded Whether stale data should be refreshed when an active observer is added.
 * @property revalidateOnNetworkConnection Whether stale data should be refreshed when a network connection is established and the replica has an active observer. Note: [NetworkConnectivityProvider] must be added to [ReplicaClient].
 */
data class ReplicaSettings(
    val staleTime: Duration?,
    val clearTime: Duration? = null,
    val clearErrorTime: Duration? = 250.milliseconds,
    val cancelTime: Duration? = 250.milliseconds,
    val revalidateOnActiveObserverAdded: Boolean = true,
    val revalidateOnNetworkConnection: Boolean = true
) {

    companion object {
        /**
         * Settings for a replica with no automatic behaviour.
         */
        val WithoutBehaviour = ReplicaSettings(
            staleTime = null,
            clearTime = null,
            clearErrorTime = null,
            cancelTime = null,
            revalidateOnActiveObserverAdded = false,
            revalidateOnNetworkConnection = false
        )
    }
}
