package me.aartikov.replica.common

/**
 * Represent several errors (one or more) occurred during a network request.
 *
 * Multiple errors can occur in a combined replica. See replica-algebra module for more details.
 */
data class CombinedLoadingError(val errors: List<LoadingError>) {

    constructor(reason: LoadingReason, exception: Exception) : this(listOf(LoadingError(reason, exception)))

    init {
        check(errors.isNotEmpty()) { "Attempt to create empty CombinedLoadingError" }
    }

    /**
     * Returns first exception. Can be used when UI is not suitable to display multiple errors at
     * the same time.
     */
    val exception get(): Exception = errors[0].exception

    /**
     * Returns first [LoadingReason]
     */
    val reason get(): LoadingReason = errors[0].reason
}