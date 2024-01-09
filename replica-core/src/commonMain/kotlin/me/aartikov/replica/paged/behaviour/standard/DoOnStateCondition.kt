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
class DoOnStateCondition<T : Any, P : Page<T>>(
    private val condition: (PagedReplicaState<T, P>) -> Boolean,
    private val startDelay: Duration = Duration.ZERO,
    private val repeatInterval: Duration? = null,
    private val action: suspend PagedPhysicalReplica<T, P>.() -> Unit
) : PagedReplicaBehaviour<T, P> {

    private var job: Job? = null

    override fun setup(replica: PagedPhysicalReplica<T, P>) {
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

    private fun CoroutineScope.launchJob(replica: PagedPhysicalReplica<T, P>) {
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