package me.aartikov.replica.simple

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

interface Replica<T : Any> {

    fun observe(
        observerCoroutineScope: CoroutineScope,
        observerActiveFlow: StateFlow<Boolean>
    ): ReplicaObserver<T>

    fun refresh()

    suspend fun getData(): T

    suspend fun getRefreshedData(): T
}