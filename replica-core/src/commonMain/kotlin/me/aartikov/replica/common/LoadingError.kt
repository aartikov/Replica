package me.aartikov.replica.common

/**
 * Represent an error occurred during a network request.
 */
data class LoadingError(
    val reason: LoadingReason,
    val exception: Exception
)