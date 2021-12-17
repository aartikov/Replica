package me.aartikov.replica.sample.core.data

import me.aartikov.replica.simple.ReplicaSettings
import kotlin.time.Duration.Companion.seconds

val GlobalReplicaSettings = ReplicaSettings.Default.copy(
    staleTime = 5.seconds
)