package me.aartikov.replica.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import me.aartikov.replica.client.ReplicaClient.Companion.DefaultCoroutineScope
import me.aartikov.replica.client.internal.ReplicaClientImpl
import me.aartikov.replica.common.ReplicaAction
import me.aartikov.replica.common.ReplicaTag
import me.aartikov.replica.keyed.KeyedFetcher
import me.aartikov.replica.keyed.KeyedPhysicalReplica
import me.aartikov.replica.keyed.KeyedReplicaSettings
import me.aartikov.replica.keyed.KeyedStorage
import me.aartikov.replica.keyed.behaviour.KeyedReplicaBehaviour
import me.aartikov.replica.keyed_paged.KeyedPagedFetcher
import me.aartikov.replica.keyed_paged.KeyedPagedPhysicalReplica
import me.aartikov.replica.keyed_paged.KeyedPagedReplicaSettings
import me.aartikov.replica.keyed_paged.behaviour.KeyedPagedReplicaBehaviour
import me.aartikov.replica.network.NetworkConnectivityProvider
import me.aartikov.replica.paged.Page
import me.aartikov.replica.paged.PagedFetcher
import me.aartikov.replica.paged.PagedPhysicalReplica
import me.aartikov.replica.paged.PagedReplicaSettings
import me.aartikov.replica.paged.behaviour.PagedReplicaBehaviour
import me.aartikov.replica.single.Fetcher
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.single.Storage
import me.aartikov.replica.single.behaviour.ReplicaBehaviour
import me.aartikov.replica.single.behaviour.standard.doOnAction
import me.aartikov.replica.single.behaviour.standard.mutateOnAction
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
     * Emits [ReplicaAction]s.
     */
    val actions: Flow<ReplicaAction>

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
        settings: KeyedReplicaSettings<K, T>,
        childSettings: (K) -> ReplicaSettings,
        tags: Set<ReplicaTag> = emptySet(),
        childTags: (K) -> Set<ReplicaTag> = { emptySet() },
        behaviours: List<KeyedReplicaBehaviour<K, T>> = emptyList(),
        childBehaviours: (K) -> List<ReplicaBehaviour<T>> = { emptyList() },
        storage: KeyedStorage<K, T>? = null,
        fetcher: KeyedFetcher<K, T>
    ): KeyedPhysicalReplica<K, T>

    /**
     * Creates a [PagedPhysicalReplica].
     * Note: once created a replica will exist as long as a client exists.
     *
     * @param name a human readable replica name. Can be used for debugging (for example it is used by Replica DevTools). Shouldn't be unique.
     * @param settings configures replica behaviour. See: [PagedReplicaSettings].
     * @param tags set of [ReplicaTag]s. Can be used to perform bulk operations on a subset of replicas. See: [cancelByTags], [clearByTags], [invalidateByTags].
     * @param idExtractor configures how to extract unique ids from items to remove accidental
     * duplicates. Pass null if you don't need it.
     * @param behaviours allow to add custom behaviours to a replica. See: [PagedReplicaBehaviour].
     * @param fetcher configures how to load data from a network. See: [PagedFetcher].
     */
    fun <I : Any, P : Page<I>> createPagedReplica(
        name: String,
        settings: PagedReplicaSettings,
        tags: Set<ReplicaTag> = emptySet(),
        idExtractor: ((I) -> Any)?,
        behaviours: List<PagedReplicaBehaviour<I, P>> = emptyList(),
        fetcher: PagedFetcher<I, P>
    ): PagedPhysicalReplica<I, P>

    /**
     * Creates a [KeyedPagedPhysicalReplica].
     * Note: once created a keyed paged replica will exist as long as a client exists.
     *
     * @param name a human readable keyed paged replica name. Can be used for debugging
     * (for example it is used by Replica Devtools). Shouldn't be unique.
     * @param childName names for child replicas.
     * @param settings configures keyed paged replica behaviour. See: [KeyedPagedReplicaSettings].
     * @param childSettings [PagedReplicaSettings] for child paged replicas.
     * @param tags set of [ReplicaTag]s. Can be used to perform bulk operations on a subset of
     * paged replicas. See: [cancelByTags], [clearByTags], [invalidateByTags].
     * @param idExtractor configures how to extract unique ids from items to remove accidental
     * duplicates. Pass null if you don't need it.
     * @param childTags tags for child paged replicas.
     * @param behaviours allow to add custom behaviours to a keyed paged replica.
     * See: [KeyedPagedReplicaBehaviour].
     * @param childBehaviours custom behaviours for child paged replicas.
     * @param fetcher configures how to loads data from a network. See: [KeyedPagedFetcher].
     */
    fun <K : Any, I : Any, P : Page<I>> createKeyedPagedReplica(
        name: String,
        childName: (K) -> String,
        settings: KeyedPagedReplicaSettings<K, I, P>,
        childSettings: (K) -> PagedReplicaSettings,
        tags: Set<ReplicaTag> = emptySet(),
        childTags: (K) -> Set<ReplicaTag> = { emptySet() },
        idExtractor: ((I) -> Any)?,
        behaviours: List<KeyedPagedReplicaBehaviour<K, I, P>> = emptyList(),
        childBehaviours: (K) -> List<PagedReplicaBehaviour<I, P>> = { emptyList() },
        fetcher: KeyedPagedFetcher<K, I, P>
    ): KeyedPagedPhysicalReplica<K, I, P>

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

    /**
     * Executes an [action] on each [PagedPhysicalReplica].
     *
     * @param includeChildrenOfKeyedReplicas specifies if an action will be executed on children of keyed replicas.
     */
    suspend fun onEachPagedReplica(
        includeChildrenOfKeyedReplicas: Boolean = true,
        action: suspend PagedPhysicalReplica<*, *>.() -> Unit
    )

    /**
     * Executes an [action] on each [KeyedPagedPhysicalReplica].
     */
    suspend fun onEachKeyedPagedReplica(
        action: suspend KeyedPagedPhysicalReplica<*, *, *>.() -> Unit
    )

    /**
     * Sends an [action]. Use [doOnAction] or [mutateOnAction] to handle it.
     */
    fun sendAction(action: ReplicaAction)
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