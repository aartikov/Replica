package me.aartikov.replica.common

data class CombinedLoadingError(val exceptions: List<Exception>) {

    constructor(exception: Exception) : this(listOf(exception))

    init {
        check(exceptions.isNotEmpty()) { "Attempt to create empty CombinedLoadingError" }
    }

    val firstException get() = exceptions[0]
}