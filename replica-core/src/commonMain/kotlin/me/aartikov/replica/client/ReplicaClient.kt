package me.aartikov.replica.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import me.aartikov.replica.client.ReplicaClient.Companion.DefaultCoroutineScope
import me.aartikov.replica.client.internal.ReplicaClientImpl
import me.aartikov.replica.common.ReplicaTag
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
import me.aartikov.replica.time.RealTimeProvider
import me.aartikov.replica.time.TimeProvider

/**
 * Creates and manages replicas.
 */
interface ReplicaClient {

    companion object {
        val DefaultCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }

    /**
     * A coroutine scope that represents life time of a client.
     */
    val coroutineScope: CoroutineScope

    /**
     * Returns [NetworkConnectivityProvider] that was passed to create a client.
     */
    val networkConnectivityProvider: NetworkConnectivityProvider?

    /**
     * Notifies that some [ReplicaClientEvent] has occurred.
     */
    val eventFlow: Flow<ReplicaClientEvent>

    /**
     * Creates a [PhysicalReplica].
     * Note: once created a replica will exist as long as a client exists.
     *
     * @param name a human readable replica name. Can be used for debugging (for example it is used by Replica DevTools). Shouldn't be unique.
     * @param settings configures replica behaviour. See: [ReplicaSettings].
     * @param tags set of [ReplicaTag]s. Can be used to perform bulk operations on a subset of replicas. See: [cancelByTags], [clearByTags], [invalidateByTags].
     * @param behaviours allow to add custom behaviours to a replica. See: [ReplicaBehaviour].
     * @param storage makes replica data persistent. See: [Storage].
     * @param fetcher configures how to load data from a network. See: [Fetcher].
     */
    fun <T : Any> createReplica(
        name: String,
        settings: ReplicaSettings,
        tags: Set<ReplicaTag> = emptySet(),
        behaviours: List<ReplicaBehaviour<T>> = emptyList(),
        storage: Storage<T>? = null,
        fetcher: Fetcher<T>
    ): PhysicalReplica<T>

    /**
     * Creates a [KeyedPhysicalReplica].
     * Note: once created a keyed replica will exist as long as a client exists.
     *
     * @param name a human readable keyed replica name. Can be used for debugging (for example it is used by Replica Devtools). Shouldn't be unique.
     * @param childName names for child replicas.
     * @param settings configures keyed replica behaviour. See: [KeyedReplicaSettings].
     * @param childSettings [ReplicaSettings] for child replicas.
     * @param tags set of [ReplicaTag]s. Can be used to perform bulk operations on a subset of keyed replicas. See: [cancelByTags], [clearByTags], [invalidateByTags].
     * @param childTags tags for child replicas.
     * @param behaviours allow to add custom behaviours to a keyed replica. See: [KeyedReplicaBehaviour].
     * @param childBehaviours custom behaviours for child replicas.
     * @param storage makes keyed replica data persistent. See: [KeyedStorage].
     * @param fetcher configures how to loads data from a network. See: [KeyedFetcher].
     */
    fun <K : Any, T : Any> createKeyedReplica(
        name: String,
        childName: (K) -> String,
        settings: KeyedReplicaSettings<K, T> = KeyedReplicaSettings(),
        childSettings: (K) -> ReplicaSettings,
        tags: Set<ReplicaTag> = emptySet(),
        childTags: (K) -> Set<ReplicaTag> = { emptySet() },
        behaviours: List<KeyedReplicaBehaviour<K, T>> = emptyList(),
        childBehaviours: (K) -> List<ReplicaBehaviour<T>> = { emptyList() },
        storage: KeyedStorage<K, T>? = null,
        fetcher: KeyedFetcher<K, T>
    ): KeyedPhysicalReplica<K, T>

    /**
     * Executes an [action] on each [PhysicalReplica].
     *
     * @param includeChildrenOfKeyedReplicas specifies if an action will be executed on children of keyed replicas.
     */
    suspend fun onEachReplica(
        includeChildrenOfKeyedReplicas: Boolean = true,
        action: suspend PhysicalReplica<*>.() -> Unit
    )

    /**
     * Executes an [action] on each [KeyedPhysicalReplica].
     */
    suspend fun onEachKeyedReplica(
        action: suspend KeyedPhysicalReplica<*, *>.() -> Unit
    )
}

/**
 * Creates a replica client. Typically, it should be a singleton.
 *
 * @param networkConnectivityProvider See: [NetworkConnectivityProvider]
 * @param timeProvider See: [TimeProvider]
 * @param coroutineScope a coroutine scope that represents life time of a replica client. This scope must have a single-thread coroutine dispatcher for example [Dispatchers.Main.immediate].
 */
fun ReplicaClient(
    networkConnectivityProvider: NetworkConnectivityProvider? = null,
    timeProvider: TimeProvider = RealTimeProvider(),
    coroutineScope: CoroutineScope = DefaultCoroutineScope
): ReplicaClient {
    return ReplicaClientImpl(
        networkConnectivityProvider,
        timeProvider,
        coroutineScope
    )
}