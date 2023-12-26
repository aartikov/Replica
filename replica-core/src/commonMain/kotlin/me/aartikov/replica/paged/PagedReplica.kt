package me.aartikov.replica.paged

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

interface PagedReplica<out T : Any, out P : Page<T>> {

    fun observe(
        observerCoroutineScope: CoroutineScope,
        observerActive: StateFlow<Boolean>
    ): PagedReplicaObserver<T, P>

    fun refresh()

    fun revalidate()

}