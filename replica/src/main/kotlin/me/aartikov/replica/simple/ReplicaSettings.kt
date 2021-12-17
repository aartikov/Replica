package me.aartikov.replica.simple

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class ReplicaSettings(
    val revalidateOnActivated: Boolean,
    val staleTime: Duration?,
    val clearTime: Duration?,
    val cancelTime: Duration?
) {

    companion object {

        val Default = ReplicaSettings(
            revalidateOnActivated = true,
            staleTime = 10.seconds,
            clearTime = null,
            cancelTime = 0.5.seconds
        )

        val WithoutBehaviour = ReplicaSettings(
            revalidateOnActivated = false,
            staleTime = null,
            clearTime = null,
            cancelTime = null
        )
    }
}