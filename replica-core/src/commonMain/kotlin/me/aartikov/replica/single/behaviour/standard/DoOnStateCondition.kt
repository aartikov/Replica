package me.aartikov.replica.single.behaviour.standard

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaState
import me.aartikov.replica.single.behaviour.ReplicaBehaviour
import kotlin.time.Duration

/**
 * [ReplicaBehaviour] that executes an [action] when a state satisfies a [condition].
 * [startDelay] allows to delay execution of an action. If a state stops satisfying the condition before [startDelay] expires the action will not be executed.
 * [repeatInterval] allows to execute an [action] periodically until a state satisfying the condition. null means execute the action once.
 */
fun <T : Any> ReplicaBehaviour.Companion.doOnStateCondition(
    condition: (ReplicaState<T>) -> Boolean,
    startDelay: Duration = Duration.ZERO,
    repeatInterval: Duration? = null,
    action: suspend PhysicalReplica<T>.() -> Unit
): ReplicaBehaviour<T> = DoOnStateCondition(condition, startDelay, repeatInterval, action)

private class DoOnStateCondition<T : Any>(
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