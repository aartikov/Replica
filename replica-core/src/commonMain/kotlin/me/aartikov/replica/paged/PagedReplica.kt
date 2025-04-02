package me.aartikov.replica.paged

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.common.ReplicaObserverHost

interface PagedReplica<out T : Any> {

    fun observe(observerHost: ReplicaObserverHost): PagedReplicaObserver<T>

    @Deprecated("Use observe(observerHost) instead")
    fun observe(
        observerCoroutineScope: CoroutineScope,
        observerActive: StateFlow<Boolean>
    ): PagedReplicaObserver<T> =
        observe(ReplicaObserverHost(observerCoroutineScope, observerActive))

    fun refresh()

    fun revalidate()

    fun loadNext()

    fun loadPrevious()

}