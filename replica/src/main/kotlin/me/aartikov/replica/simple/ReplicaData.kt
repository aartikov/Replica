package me.aartikov.replica.simple

data class ReplicaData<out T : Any>(
    val value: T,
    val fresh: Boolean
)