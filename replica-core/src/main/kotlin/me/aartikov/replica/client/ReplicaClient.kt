package me.aartikov.replica.client

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import me.aartikov.replica.client.ReplicaClient.Companion.DefaultCoroutineDispatcher
import me.aartikov.replica.client.ReplicaClient.Companion.DefaultCoroutineScope
import me.aartikov.replica.client.internal.ReplicaClientImpl
import me.aartikov.replica.keyed.KeyedFetcher
import me.aartikov.replica.keyed.KeyedPhysicalReplica
import me.aartikov.replica.keyed.KeyedReplicaSettings
import me.aartikov.replica.keyed.KeyedStorage
import me.aartikov.replica.keyed.behaviour.KeyedReplicaBehaviour
import me.aartikov.replica.network.NetworkConnectivityProvider
import me.aartikov.replica.single.Fetcher
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.single.Storage
import me.aartikov.replica.single.behaviour.ReplicaBehaviour

interface ReplicaClient {

    companion object {
        val DefaultCoroutineDispatcher = Dispatchers.Main.immediate
        val DefaultCoroutineScope = CoroutineScope(SupervisorJob() + DefaultCoroutineDispatcher)
    }

    val coroutineScope: CoroutineScope

    val networkConnectivityProvider: NetworkConnectivityProvider?

    val eventFlow: Flow<ReplicaClientEvent>

    fun <T : Any> createReplica(
        name: String,
        settings: ReplicaSettings,
        behaviours: List<ReplicaBehaviour<T>> = emptyList(),
        storage: Storage<T>? = null,
        fetcher: Fetcher<T>,
    ): PhysicalReplica<T>

    fun <K : Any, T : Any> createKeyedReplica(
        name: String,
        childName: (K) -> String,
        settings: KeyedReplicaSettings<K, T> = KeyedReplicaSettings(),
        childSettings: (K) -> ReplicaSettings,
        behaviours: List<KeyedReplicaBehaviour<K, T>> = emptyList(),
        childBehaviours: (K) -> List<ReplicaBehaviour<T>> = { emptyList() },
        storage: KeyedStorage<K, T>? = null,
        fetcher: KeyedFetcher<K, T>
    ): KeyedPhysicalReplica<K, T>

    suspend fun onEachReplica(
        includeChildrenOfKeyedReplicas: Boolean = true,
        action: suspend PhysicalReplica<*>.() -> Unit
    )

    suspend fun onEachKeyedReplica(
        action: suspend KeyedPhysicalReplica<*, *>.() -> Unit
    )
}

fun ReplicaClient(
    networkConnectivityProvider: NetworkConnectivityProvider? = null,
    coroutineDispatcher: CoroutineDispatcher = DefaultCoroutineDispatcher,
    coroutineScope: CoroutineScope = DefaultCoroutineScope
): ReplicaClient {
    return ReplicaClientImpl(networkConnectivityProvider, coroutineDispatcher, coroutineScope)
}