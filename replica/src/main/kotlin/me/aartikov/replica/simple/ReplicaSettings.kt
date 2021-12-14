package me.aartikov.replica.simple

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

data class ReplicaSettings(
    val loadDataOnActiveObserverAdded: Boolean,
    val staleTime: Duration?
) {

    companion object {
        val Default = ReplicaSettings(
            loadDataOnActiveObserverAdded = true,
            staleTime = 1.minutes
        )

        val WithoutBehaviour = ReplicaSettings(
            loadDataOnActiveObserverAdded = false,
            staleTime = null
        )
    }
}