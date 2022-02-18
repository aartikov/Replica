package me.aartikov.replica.algebra.utils

import me.aartikov.replica.keyed.KeyedFetcher
import me.aartikov.replica.keyed.KeyedPhysicalReplica
import me.aartikov.replica.keyed.KeyedReplicaSettings
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.time.TimeProvider

class KeyedReplicaProvider {

    private val timeProvider = FakeTimeProvider()

    private val defaultReplicaSettings = KeyedReplicaSettings<Int, String>()

    private val defaultChildReplicaSettings = { _: Int -> ReplicaSettings.WithoutBehaviour }

    private val defaultFetcher = KeyedFetcher<Int, String> { id -> testData(id) }

    private val clientProvider = ReplicaClientProvider()

    companion object {
        val testData = { id: Int -> "test_$id" }
    }

    fun replica(
        timeProvider: TimeProvider = this.timeProvider,
        fetcher: KeyedFetcher<Int, String> = defaultFetcher,
        replicaSettings: KeyedReplicaSettings<Int, String> = defaultReplicaSettings,
        childReplicaSettings: (Int) -> ReplicaSettings = defaultChildReplicaSettings
    ): KeyedPhysicalReplica<Int, String> {
        val replicaClient = clientProvider.client(
            timeProvider = timeProvider
        )

        return replicaClient.createKeyedReplica(
            name = "test",
            settings = replicaSettings,
            childName = { "child_replica_$it" },
            childSettings = childReplicaSettings,
            fetcher = fetcher
        )
    }
}