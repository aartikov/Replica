package me.aartikov.replica.client.internal

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.keyed.KeyedFetcher
import me.aartikov.replica.keyed.KeyedPhysicalReplica
import me.aartikov.replica.keyed.KeyedStorage
import me.aartikov.replica.keyed.internal.KeyedPhysicalReplicaImpl
import me.aartikov.replica.keyed.internal.SingleKeyStorage
import me.aartikov.replica.network.NetworkConnectivityProvider
import me.aartikov.replica.single.Fetcher
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.single.Storage
import me.aartikov.replica.single.behaviour.ReplicaBehaviour
import me.aartikov.replica.single.behaviour.standard.createStandardBehaviours
import me.aartikov.replica.single.internal.PhysicalReplicaImpl

internal class ReplicaClientImpl(
    override val networkConnectivityProvider: NetworkConnectivityProvider?,
    private val coroutineDispatcher: CoroutineDispatcher,
    private val coroutineScope: CoroutineScope
) : ReplicaClient {

    private val replicas = concurrentHashSetOf<PhysicalReplica<*>>()
    private val keyedReplicas = concurrentHashSetOf<KeyedPhysicalReplica<*, *>>()

    override fun <T : Any> createReplica(
        settings: ReplicaSettings,
        behaviours: List<ReplicaBehaviour<T>>,
        storage: Storage<T>?,
        fetcher: Fetcher<T>
    ): PhysicalReplica<T> {
        val replica = createReplicaInternal(
            settings,
            behaviours,
            storage,
            fetcher,
            coroutineDispatcher,
            coroutineScope
        )
        replicas.add(replica)
        return replica
    }

    override fun <K : Any, T : Any> createKeyedReplica(
        settings: (K) -> ReplicaSettings,
        behaviours: (K) -> List<ReplicaBehaviour<T>>,
        storage: KeyedStorage<K, T>?,
        fetcher: KeyedFetcher<K, T>
    ): KeyedPhysicalReplica<K, T> {
        val replicaFactory =
            { childCoroutineScope: CoroutineScope, key: K ->
                createReplicaInternal(
                    settings = settings(key),
                    behaviours = behaviours(key),
                    storage = storage?.let { SingleKeyStorage(it, key) },
                    fetcher = { fetcher.fetch(key) },
                    coroutineDispatcher = coroutineDispatcher,
                    coroutineScope = childCoroutineScope
                )
            }

        val keyedReplica = KeyedPhysicalReplicaImpl(coroutineScope, storage, replicaFactory)
        keyedReplicas.add(keyedReplica)
        return keyedReplica
    }

    override suspend fun onEachReplica(
        includeChildrenOfKeyedReplicas: Boolean,
        action: suspend PhysicalReplica<*>.() -> Unit
    ) {
        replicas.forEach {
            it.action()
        }

        if (includeChildrenOfKeyedReplicas) {
            keyedReplicas.forEach {
                it.onEachReplica {
                    action()
                }
            }
        }
    }

    override suspend fun onEachKeyedReplica(action: suspend KeyedPhysicalReplica<*, *>.() -> Unit) {
        keyedReplicas.forEach {
            it.action()
        }
    }

    private fun <T : Any> createReplicaInternal(
        settings: ReplicaSettings,
        behaviours: List<ReplicaBehaviour<T>>,
        storage: Storage<T>?,
        fetcher: Fetcher<T>,
        coroutineDispatcher: CoroutineDispatcher,
        coroutineScope: CoroutineScope
    ): PhysicalReplica<T> {

        validateSettings(settings, hasStorage = storage != null)

        val standardBehaviours = createStandardBehaviours<T>(settings, networkConnectivityProvider)

        return PhysicalReplicaImpl(
            coroutineDispatcher,
            coroutineScope,
            behaviours = standardBehaviours + behaviours,
            storage,
            fetcher
        )
    }

    private fun validateSettings(settings: ReplicaSettings, hasStorage: Boolean) {
        if (hasStorage && settings.clearTime != null) {
            throw IllegalArgumentException("clearTime is not supported for replicas with storage")
        }
    }
}