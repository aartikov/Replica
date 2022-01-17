package me.aartikov.replica.single.behaviour.standard

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.single.*
import me.aartikov.replica.single.behaviour.ReplicaBehaviour
import kotlin.time.Duration

internal class CancellationBehaviour<T : Any>(
    private val cancelTime: Duration
) : ReplicaBehaviour<T> {

    private var cancellationJob: Job? = null

    override fun setup(replica: PhysicalReplica<T>) {
        replica.eventFlow
            .onEach { event ->
                when (event) {
                    is ReplicaEvent.ObserverCountChangedEvent,
                    is ReplicaEvent.LoadingEvent.LoadingStarted,
                    is ReplicaEvent.LoadingEvent.LoadingFinished -> {
                        if (replica.currentState.canBeCanceled) {
                            replica.coroutineScope.launchCancellationJob(replica)
                        } else {
                            cancelCancellationJob()
                        }
                    }
                    else -> Unit
                }
            }
            .launchIn(replica.coroutineScope)
    }

    private val ReplicaState<T>.canBeCanceled: Boolean
        get() = observingStatus == ObservingStatus.None && loading && !dataRequested

    private fun CoroutineScope.launchCancellationJob(replica: PhysicalReplica<T>) {
        if (cancellationJob?.isActive == true) return

        cancellationJob = launch {
            delay(cancelTime.inWholeMilliseconds)
            withContext(NonCancellable) {
                replica.cancelLoading()
            }
        }
    }

    private fun cancelCancellationJob() {
        cancellationJob?.cancel()
        cancellationJob = null
    }
}