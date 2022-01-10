package me.aartikov.replica.single

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class ReplicaSettings(
    val staleTime: Duration?,
    val clearTime: Duration? = null,
    val clearErrorTime: Duration? = 250.milliseconds,
    val cancelTime: Duration? = 250.milliseconds,
    val revalidateOnActivated: Boolean = true,
    val revalidateOnNetworkConnection: RevalidateAction = RevalidateAction.RevalidateIfHasActiveObservers,
    val refreshOnStale: RefreshAction = RefreshAction.DontRefresh
) {

    companion object {
        val WithoutBehaviour = ReplicaSettings(
            staleTime = null,
            clearTime = null,
            clearErrorTime = null,
            cancelTime = null,
            revalidateOnActivated = false
        )
    }
}