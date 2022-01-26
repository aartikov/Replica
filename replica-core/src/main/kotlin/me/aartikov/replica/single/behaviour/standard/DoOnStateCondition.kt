package me.aartikov.replica.single.behaviour.standard

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaState
import me.aartikov.replica.single.behaviour.ReplicaBehaviour
import kotlin.time.Duration

class DoOnStateCondition<T : Any>(
    private val condition: (ReplicaState<T>) -> Boolean,
    private val startDelay: Duration = Duration.ZERO,
    private val repeatInterval: Duration? = null,
    private val action: suspend PhysicalReplica<T>.() -> Unit
) : ReplicaBehaviour<T> {

    private var job: Job? = null

    override fun setup(replica: PhysicalReplica<T>) {
        replica.stateFlow
            .map { condition(it) }
            .distinctUntilChanged()
            .onEach { conditionsMet ->
                if (conditionsMet) {
                    replica.coroutineScope.launchJob(replica)
                } else {
                    cancelJob()
                }
            }
            .launchIn(replica.coroutineScope)
    }

    private fun CoroutineScope.launchJob(replica: PhysicalReplica<T>) {
        if (job?.isActive == true) return

        job = launch {
            delay(startDelay.inWholeMilliseconds)
            while (true) {
                withContext(NonCancellable) {
                    replica.action()
                }
                if (repeatInterval != null) {
                    delay(repeatInterval.inWholeMilliseconds)
                } else {
                    break
                }
            }
        }
    }

    private fun cancelJob() {
        job?.cancel()
        job = null
    }
}