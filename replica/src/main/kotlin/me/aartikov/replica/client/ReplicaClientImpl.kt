package me.aartikov.replica.client

import kotlinx.coroutines.CoroutineScope
import me.aartikov.replica.simple.CoreReplica
import me.aartikov.replica.simple.Fetcher
import me.aartikov.replica.simple.Replica
import me.aartikov.replica.simple.ReplicaSettings
import me.aartikov.replica.simple.behaviour.ReplicaBehaviour
import me.aartikov.replica.simple.behaviour.createStandardBehaviours
import me.aartikov.replica.simple.internal.ReplicaImpl
import java.util.*
import java.util.concurrent.ConcurrentHashMap

internal class ReplicaClientImpl(
    override val replicaSettings: ReplicaSettings,
    override val coroutineScope: CoroutineScope
) : ReplicaClient {

    private val replicas = Collections.newSetFromMap(ConcurrentHashMap<Replica<*>, Boolean>())

    override fun <T : Any> createReplica(
        settings: ReplicaSettings,
        behaviours: List<ReplicaBehaviour<T>>,
        coroutineScope: CoroutineScope,
        fetcher: Fetcher<T>
    ): CoreReplica<T> {
        val replica = ReplicaImpl<T>(
            behaviours = createStandardBehaviours<T>(settings) + behaviours,
            coroutineScope,
            fetcher,
            onFinished = this::removeReplica
        )
        replicas.add(replica)
        return replica
    }

    private fun removeReplica(replica: ReplicaImpl<*>) {
        replicas.remove(replica)
    }
}