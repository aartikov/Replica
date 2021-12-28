package me.aartikov.replica.single.behaviour.standard

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaEvent
import me.aartikov.replica.single.ReplicaState
import me.aartikov.replica.single.behaviour.ReplicaBehaviour
import me.aartikov.replica.single.currentState
import kotlin.time.Duration

internal class CancellationBehaviour<T : Any>(
    private val cancelTime: Duration
) : ReplicaBehaviour<T> {

    private var cancellationJob: Job? = null

    override fun setup(coroutineScope: CoroutineScope, replica: PhysicalReplica<T>) {
        replica.eventFlow
            .onEach { event ->
                when (event) {
                    is ReplicaEvent.ObserverCountChanged,
                    is ReplicaEvent.LoadingEvent -> {
                        if (replica.currentState.canBeCanceled) {
                            coroutineScope.launchCancellationJob(replica)
                        } else {
                            cancelCancellationJob()
                        }
                    }
                    else -> Unit
                }
            }
            .launchIn(coroutineScope)
    }

    private val ReplicaState<T>.canBeCanceled: Boolean get() = observerCount == 0 && loading && !dataRequested

    private fun CoroutineScope.launchCancellationJob(replica: PhysicalReplica<T>) {
        if (cancellationJob?.isActive == true) return

        cancellationJob = launch {
            delay(cancelTime.inWholeMilliseconds)
            replica.cancelLoading()
        }
    }

    private fun cancelCancellationJob() {
        cancellationJob?.cancel()
        cancellationJob = null
    }
}