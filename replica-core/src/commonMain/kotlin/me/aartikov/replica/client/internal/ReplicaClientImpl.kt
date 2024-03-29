package me.aartikov.replica.client.internal

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.client.ReplicaClientEvent
import me.aartikov.replica.common.ReplicaTag
import me.aartikov.replica.common.internal.Lock
import me.aartikov.replica.common.internal.withLock
import me.aartikov.replica.keyed.KeyedFetcher
import me.aartikov.replica.keyed.KeyedPhysicalReplica
import me.aartikov.replica.keyed.KeyedReplicaSettings
import me.aartikov.replica.keyed.KeyedStorage
import me.aartikov.replica.keyed.behaviour.KeyedReplicaBehaviour
import me.aartikov.replica.keyed.behaviour.createBehavioursForKeyedReplicaSettings
import me.aartikov.replica.keyed.internal.FixedKeyStorage
import me.aartikov.replica.keyed.internal.KeyedPhysicalReplicaImpl
import me.aartikov.replica.keyed.internal.KeyedStorageCleaner
import me.aartikov.replica.network.NetworkConnectivityProvider
import me.aartikov.replica.single.Fetcher
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.single.Storage
import me.aartikov.replica.single.behaviour.ReplicaBehaviour
import me.aartikov.replica.single.behaviour.createBehavioursForReplicaSettings
import me.aartikov.replica.single.internal.PhysicalReplicaImpl
import me.aartikov.replica.single.internal.SequentialStorage
import me.aartikov.replica.time.TimeProvider

internal class ReplicaClientImpl(
    override val networkConnectivityProvider: NetworkConnectivityProvider?,
    private val timeProvider: TimeProvider,
    override val coroutineScope: CoroutineScope
) : ReplicaClient {

    @OptIn(ExperimentalStdlibApi::class)
    private val coroutineDispatcher = coroutineScope.coroutineContext[CoroutineDispatcher.Key]!!

    private val _eventFlow = MutableSharedFlow<ReplicaClientEvent>(extraBufferCapacity = 1000)
    override val eventFlow get() = _eventFlow.asSharedFlow()

    private val replicasLock = Lock()
    private val replicas = mutableSetOf<PhysicalReplica<*>>()

    private val keyedReplicasLock = Lock()
    private val keyedReplicas = mutableSetOf<KeyedPhysicalReplica<*, *>>()

    override fun <T : Any> createReplica(
        name: String,
        settings: ReplicaSettings,
        tags: Set<ReplicaTag>,
        behaviours: List<ReplicaBehaviour<T>>,
        storage: Storage<T>?,
        fetcher: Fetcher<T>
    ): PhysicalReplica<T> {
        val replica = createReplicaInternal(
            name,
            settings,
            tags,
            behaviours,
            storage?.let { SequentialStorage(it) },
            fetcher,
            coroutineDispatcher,
            coroutineScope
        )
        replicasLock.withLock {
            replicas.add(replica)
        }
        _eventFlow.tryEmit(ReplicaClientEvent.ReplicaCreated(replica))
        return replica
    }

    override fun <K : Any, T : Any> createKeyedReplica(
        name: String,
        childName: (K) -> String,
        settings: KeyedReplicaSettings<K, T>,
        childSettings: (K) -> ReplicaSettings,
        tags: Set<ReplicaTag>,
        childTags: (K) -> Set<ReplicaTag>,
        behaviours: List<KeyedReplicaBehaviour<K, T>>,
        childBehaviours: (K) -> List<ReplicaBehaviour<T>>,
        storage: KeyedStorage<K, T>?,
        fetcher: KeyedFetcher<K, T>
    ): KeyedPhysicalReplica<K, T> {

        validateKeyedSettings(settings, hasStorage = storage != null)

        val storageCleaner = storage?.let { KeyedStorageCleaner(it) }

        val replicaFactory = { childCoroutineScope: CoroutineScope, key: K ->
            createReplicaInternal(
                name = childName(key),
                settings = childSettings(key),
                tags = childTags(key),
                behaviours = childBehaviours(key),
                storage = storage?.let {
                    SequentialStorage(
                        FixedKeyStorage(it, key),
                        additionalMutex = storageCleaner?.mutex
                    )
                },
                fetcher = { fetcher.fetch(key) },
                coroutineDispatcher = coroutineDispatcher,
                coroutineScope = childCoroutineScope
            )
        }

        val behavioursForSettings = createBehavioursForKeyedReplicaSettings<K, T>(settings)

        val keyedReplica = KeyedPhysicalReplicaImpl(
            coroutineScope,
            name,
            settings,
            tags,
            behavioursForSettings + behaviours,
            storageCleaner,
            replicaFactory
        )

        keyedReplicasLock.withLock {
            keyedReplicas.add(keyedReplica)
        }
        _eventFlow.tryEmit(ReplicaClientEvent.KeyedReplicaCreated(keyedReplica))
        return keyedReplica
    }

    override suspend fun onEachReplica(
        includeChildrenOfKeyedReplicas: Boolean,
        action: suspend PhysicalReplica<*>.() -> Unit
    ) {
        // make a copy for concurrent modification
        val replicasCopy = replicasLock.withLock {
            HashSet(replicas)
        }

        replicasCopy.forEach {
            it.action()
        }

        if (includeChildrenOfKeyedReplicas) {
            onEachKeyedReplica {
                onEachReplica {
                    action()
                }
            }
        }
    }

    override suspend fun onEachKeyedReplica(action: suspend KeyedPhysicalReplica<*, *>.() -> Unit) {
        // make a copy for concurrent modification
        val keyedReplicasCopy = keyedReplicasLock.withLock {
            HashSet(keyedReplicas)
        }
        keyedReplicasCopy.forEach {
            it.action()
        }
    }

    private fun <T : Any> createReplicaInternal(
        name: String,
        settings: ReplicaSettings,
        tags: Set<ReplicaTag>,
        behaviours: List<ReplicaBehaviour<T>>,
        storage: Storage<T>?,
        fetcher: Fetcher<T>,
        coroutineDispatcher: CoroutineDispatcher,
        coroutineScope: CoroutineScope
    ): PhysicalReplica<T> {

        validateSettings(settings, hasStorage = storage != null)

        val behavioursForSettings =
            createBehavioursForReplicaSettings<T>(settings, networkConnectivityProvider)

        return PhysicalReplicaImpl(
            timeProvider,
            coroutineDispatcher,
            coroutineScope,
            name,
            settings,
            tags,
            behaviours = behavioursForSettings + behaviours,
            storage,
            fetcher
        )
    }

    private fun validateSettings(settings: ReplicaSettings, hasStorage: Boolean) {
        if (hasStorage && settings.clearTime != null) {
            throw IllegalArgumentException("clearTime is not supported for replicas with storage.")
        }
    }

    private fun validateKeyedSettings(settings: KeyedReplicaSettings<*, *>, hasStorage: Boolean) {
        if (hasStorage && settings.maxCount != Int.MAX_VALUE) {
            throw IllegalArgumentException("maxCount is not supported for keyed replicas with storage.")
        }
    }
}