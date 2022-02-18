package me.aartikov.replica.algebra.utils

data class LoadingFailedException(override val message: String = "Loading failed") : Exception()