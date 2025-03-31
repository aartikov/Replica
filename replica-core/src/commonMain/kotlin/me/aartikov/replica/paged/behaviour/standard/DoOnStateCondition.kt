package me.aartikov.replica.paged.behaviour.standard

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
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedPhysicalReplica
import me.aartikov.replica.paged.PagedReplicaState
import me.aartikov.replica.paged.behaviour.PagedReplicaBehaviour
import kotlin.time.Duration

/**
 * [PagedReplicaBehaviour] that executes an [action] when a state satisfies a [condition].
 * [startDelay] allows to delay execution of an action. If a state stops satisfying the condition before [startDelay] expires the action will not be executed.
 * [repeatInterval] allows to execute an [action] periodically until a state satisfying the condition. null means execute the action once.
 */
fun <I : Any, P : Page<I>> PagedReplicaBehaviour.Companion.doOnStateCondition(
    condition: (PagedReplicaState<I, P>) -> Boolean,
    startDelay: Duration = Duration.ZERO,
    repeatInterval: Duration? = null,
    action: suspend PagedPhysicalReplica<I, P>.() -> Unit
): PagedReplicaBehaviour<I, P> = DoOnStateCondition(condition, startDelay, repeatInterval, action)

private class DoOnStateCondition<I : Any, P : Page<I>>(
    private val condition: (PagedReplicaState<I, P>) -> Boolean,
    private val startDelay: Duration = Duration.ZERO,
    private val repeatInterval: Duration? = null,
    private val action: suspend PagedPhysicalReplica<I, P>.() -> Unit
) : PagedReplicaBehaviour<I, P> {

    private var job: Job? = null

    override fun setup(replica: PagedPhysicalReplica<I, P>) {
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

    private fun CoroutineScope.launchJob(replica: PagedPhysicalReplica<I, P>) {
        if (job?.isActive == true) return

        job = launch {
            delay(startDelay)
            while (true) {
                withContext(NonCancellable) {
                    replica.action()
                }
                if (repeatInterval != null) {
                    delay(repeatInterval)
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