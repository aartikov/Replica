package me.aartikov.replica.paged

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.common.ReplicaObserverHost

interface PagedReplica<out T : Any> {

    fun observe(observerHost: ReplicaObserverHost): PagedReplicaObserver<T>

    fun refresh()

    fun revalidate()

    fun loadNext()

    fun loadPrevious()

}

@Deprecated("Use observe(observerHost) instead")
fun <T : Any> PagedReplica<T>.observe(
    observerCoroutineScope: CoroutineScope,
    observerActive: StateFlow<Boolean>
): PagedReplicaObserver<T> {
    return observe(ReplicaObserverHost(observerCoroutineScope, observerActive))
}