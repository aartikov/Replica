package me.aartikov.replica.client

import kotlinx.coroutines.CoroutineScope
import me.aartikov.replica.keyed.KeyedFetcher
import me.aartikov.replica.keyed.KeyedPhysicalReplica
import me.aartikov.replica.keyed.internal.KeyedPhysicalReplicaImpl
import me.aartikov.replica.single.Fetcher
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.single.Storage
import me.aartikov.replica.single.behaviour.ReplicaBehaviour
import me.aartikov.replica.single.behaviour.createStandardBehaviours
import me.aartikov.replica.single.internal.PhysicalReplicaImpl
import me.aartikov.replica.utils.concurrentHashSetOf

internal class ReplicaClientImpl(
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
        val replica = createReplicaInternal(settings, behaviours, storage, fetcher, coroutineScope)
        replicas.add(replica)
        return replica
    }

    override fun <K : Any, T : Any> createKeyedReplica(
        settings: (K) -> ReplicaSettings,
        behaviours: (K) -> List<ReplicaBehaviour<T>>,
        fetcher: KeyedFetcher<K, T>
    ): KeyedPhysicalReplica<K, T> {
        val replicaFactory = { childCoroutineScope: CoroutineScope, key: K ->
            createReplicaInternal(
                settings = settings(key),
                behaviours = behaviours(key),
                storage = null, // TODO:
                fetcher = { fetcher.fetch(key) },
                coroutineScope = childCoroutineScope
            )
        }

        val keyedReplica = KeyedPhysicalReplicaImpl(coroutineScope, replicaFactory)
        keyedReplicas.add(keyedReplica)
        return keyedReplica
    }

    override fun onEachReplica(
        includeChildrenOfKeyedReplicas: Boolean,
        action: PhysicalReplica<*>.() -> Unit
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

    override fun onEachKeyedReplica(action: KeyedPhysicalReplica<*, *>.() -> Unit) {
        keyedReplicas.forEach {
            it.action()
        }
    }

    private fun <T : Any> createReplicaInternal(
        settings: ReplicaSettings,
        behaviours: List<ReplicaBehaviour<T>>,
        storage: Storage<T>?,
        fetcher: Fetcher<T>,
        coroutineScope: CoroutineScope
    ): PhysicalReplica<T> {

        validateSettings(settings, hasStorage = storage != null)

        return PhysicalReplicaImpl(
            coroutineScope,
            behaviours = createStandardBehaviours<T>(settings) + behaviours,
            storage,
            fetcher
        )
    }

    private fun validateSettings(settings: ReplicaSettings, hasStorage: Boolean) {
        if (hasStorage && settings.clearTime != null) {
            throw IllegalArgumentException("clearTime setting is not supported for a replica with a storage")
        }
    }
}