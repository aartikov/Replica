package me.aartikov.replica.single.utils

data class LoadingFailedException(override val message: String = "Loading failed") : Exception()