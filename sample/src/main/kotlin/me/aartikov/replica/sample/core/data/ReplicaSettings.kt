package me.aartikov.replica.sample.core.data

import me.aartikov.replica.simple.ReplicaSettings
import kotlin.time.Duration.Companion.seconds

val GlobalReplicaSettings = ReplicaSettings(
    revalidateOnActivated = true,
    staleTime = 5.seconds,
    clearTime = 10.seconds
)