package me.aartikov.replica.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import me.aartikov.replica.client.ReplicaClient.Companion.DefaultCoroutineScope
import me.aartikov.replica.keyed.KeyedFetcher
import me.aartikov.replica.keyed.KeyedPhysicalReplica
import me.aartikov.replica.keyed.clearAll
import me.aartikov.replica.keyed.invalidateAll
import me.aartikov.replica.single.Fetcher
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.single.behaviour.ReplicaBehaviour
import me.aartikov.replica.single.invalidate

interface ReplicaClient {

    companion object {
        val DefaultCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }

    fun <T : Any> createReplica(
        settings: ReplicaSettings,
        behaviours: List<ReplicaBehaviour<T>> = emptyList(),
        fetcher: Fetcher<T>
    ): PhysicalReplica<T>

    fun <K : Any, T : Any> createKeyedReplica(
        settings: (K) -> ReplicaSettings,
        behaviours: (K) -> List<ReplicaBehaviour<T>> = { emptyList() },
        fetcher: KeyedFetcher<K, T>
    ): KeyedPhysicalReplica<K, T>

    fun onEachReplica(
        includeChildrenOfKeyedReplicas: Boolean = true,
        action: PhysicalReplica<*>.() -> Unit
    )

    fun onEachKeyedReplica(
        action: KeyedPhysicalReplica<*, *>.() -> Unit
    )
}

fun ReplicaClient(
    coroutineScope: CoroutineScope = DefaultCoroutineScope
): ReplicaClient {
    return ReplicaClientImpl(coroutineScope)
}

fun ReplicaClient.clearAll() {
    onEachReplica(includeChildrenOfKeyedReplicas = false) {
        clear()
    }

    onEachKeyedReplica {
        clearAll()
    }
}

fun ReplicaClient.invalidateAll() {
    onEachReplica(includeChildrenOfKeyedReplicas = false) {
        invalidate()
    }

    onEachKeyedReplica {
        invalidateAll()
    }
}