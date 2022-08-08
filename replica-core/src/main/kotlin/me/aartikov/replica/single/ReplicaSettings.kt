package me.aartikov.replica.single

import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.network.NetworkConnectivityProvider
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Configures behaviour of a replica.
 * @property staleTime specifies how quickly fetched data will became stale (null means never).
 * @property clearTime specifies how quickly data will be cleared when there is no observers (null means never).
 * @property clearErrorTime specifies how quickly error will be cleared when there is no observers (null means never).
 * @property cancelTime specifies how quickly request will be canceled when there is no observers (null means never).
 * @property revalidateOnActiveObserverAdded specifies if stale data will be refreshed when an active observer is added.
 * @property revalidateOnNetworkConnection specifies if stale data will be refreshed when a network connection is established and a replica has active observer. Note: [NetworkConnectivityProvider] has to be added to [ReplicaClient].
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
         * Settings for a replica with none automatic behaviour.
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