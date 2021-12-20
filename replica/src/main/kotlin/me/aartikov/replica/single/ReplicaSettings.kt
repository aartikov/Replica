package me.aartikov.replica.single

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class ReplicaSettings(
    val staleTime: Duration?,
    val clearTime: Duration? = null,
    val clearErrorTime: Duration? = 500.milliseconds,
    val cancelTime: Duration? = 500.milliseconds,
    val revalidateOnActivated: Boolean = true
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