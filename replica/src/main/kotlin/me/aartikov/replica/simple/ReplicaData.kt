package me.aartikov.replica.simple

data class ReplicaData<T : Any>(
    val value: T,
    val fresh: Boolean
)