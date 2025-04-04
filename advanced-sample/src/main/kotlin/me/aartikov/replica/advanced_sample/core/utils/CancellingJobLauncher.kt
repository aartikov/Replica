package me.aartikov.replica.advanced_sample.core.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.job
import me.aartikov.replica.advanced_sample.core.error_handling.ErrorHandler
import me.aartikov.replica.advanced_sample.core.error_handling.safeLaunch
import kotlin.coroutines.coroutineContext

class CancellingJobLauncher<K : Any, T : Any>(
    private val coroutineScope: CoroutineScope
) {

    private val jobMap = mutableMapOf<K, Pair<Job, T>>()

    private val _inProgressRequestIds = MutableStateFlow(emptySet<K>())

    val inProgressRequestIds = _inProgressRequestIds.asStateFlow()

    fun launchJob(
        key: K,
        targetState: T,
        errorHandler: ErrorHandler,
        onErrorHandled: ((e: Exception) -> Unit)? = null,
        block: suspend () -> Unit
    ): Job {
        // Checking if we are working with that job at the moment
        val currentJobAndTarget = jobMap[key]
        if (currentJobAndTarget != null) {
            val (currentJob, currentTarget) = currentJobAndTarget
            if (currentTarget == targetState && currentJob.isActive) {
                // Current active job is with the same target, no need to start a new one
                return currentJob
            } else if (currentTarget != targetState) {
                // Current job is with different target, cancel and start a new one
                currentJob.cancel()
            }
        }

        _inProgressRequestIds.update { it + key }

        val job = coroutineScope.safeLaunch(
            errorHandler = errorHandler,
            onErrorHandled = onErrorHandled
        ) {
            try {
                block()
            } finally {
                if (jobMap[key]?.first == coroutineContext.job) {
                    _inProgressRequestIds.update { it - key }
                    jobMap.remove(key)
                }
            }
        }

        jobMap[key] = job to targetState

        return job
    }
}