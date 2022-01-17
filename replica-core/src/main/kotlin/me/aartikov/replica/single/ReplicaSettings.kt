package me.aartikov.replica.single

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class ReplicaSettings(
    val staleTime: Duration?,
    val clearTime: Duration? = null,
    val clearErrorTime: Duration? = 250.milliseconds,
    val cancelTime: Duration? = 250.milliseconds,
    val revalidateOnActiveObserverAdded: Boolean = true,
    val revalidateOnNetworkConnection: Boolean = true
) {

    companion object {
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