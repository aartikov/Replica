package me.aartikov.replica.single

data class ReplicaData<out T : Any>(
    val value: T,
    val fresh: Boolean
)