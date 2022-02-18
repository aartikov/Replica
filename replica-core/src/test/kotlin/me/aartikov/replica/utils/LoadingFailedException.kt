package me.aartikov.replica.utils

data class LoadingFailedException(override val message: String = "Loading failed") : Exception()