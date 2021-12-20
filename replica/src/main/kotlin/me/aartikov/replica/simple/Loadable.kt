package me.aartikov.replica.simple

data class Loadable<out T : Any>(
    val data: T? = null,
    val loading: Boolean = false,
    val error: Exception? = null
)