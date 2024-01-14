package me.aartikov.replica.paged

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

interface PagedReplica<out T : Any> {

    fun observe(
        observerCoroutineScope: CoroutineScope,
        observerActive: StateFlow<Boolean>
    ): PagedReplicaObserver<T>

    fun refresh()

    fun revalidate()

    fun loadNext()

    fun loadPrevious()

}