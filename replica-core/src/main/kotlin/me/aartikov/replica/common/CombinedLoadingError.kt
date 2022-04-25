package me.aartikov.replica.common

/**
 * Represent several errors (one or more) occurred during a network request.
 *
 * Multiple errors can occur in a combined replica. See replica-algebra module for more details.
 */
data class CombinedLoadingError(val exceptions: List<Exception>) {

    constructor(exception: Exception) : this(listOf(exception))

    init {
        check(exceptions.isNotEmpty()) { "Attempt to create empty CombinedLoadingError" }
    }

    /**
     * Returns some of the errors. Can be used when UI is not suitable to display multiple errors at the same time.
     */
    val exception get() = exceptions[0]
}