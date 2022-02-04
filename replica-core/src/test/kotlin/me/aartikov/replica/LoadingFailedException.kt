package me.aartikov.replica

data class LoadingFailedException(override val message: String = "Loading failed") : Exception()