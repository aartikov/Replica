package me.aartikov.replica.paged

import me.aartikov.replica.common.ReplicaObserverHost

interface PagedReplica<out T : Any> {

    fun observe(observerHost: ReplicaObserverHost): PagedReplicaObserver<T>

    fun refresh()

    fun revalidate()

    fun loadNext()

    fun loadPrevious()

}
