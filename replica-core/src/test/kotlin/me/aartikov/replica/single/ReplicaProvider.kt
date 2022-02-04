package me.aartikov.replica.single

import kotlinx.datetime.Instant
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.time.TimeProvider

class ReplicaProvider {

    private val defaultTimeProvider = FakeTimeProvider()

    private val defaultReplicaSettings = ReplicaSettings.WithoutBehaviour

    private val defaultFetcher = Fetcher { "test" }

    fun replica(
        timeProvider: TimeProvider = defaultTimeProvider,
        fetcher: Fetcher<String> = defaultFetcher,
        replicaSettings: ReplicaSettings = defaultReplicaSettings
    ): PhysicalReplica<String> {
        val replicaClient = ReplicaClient(
            timeProvider = timeProvider
        )

        return replicaClient.createReplica(
            name = "test",
            settings = replicaSettings,
            fetcher = fetcher
        )
    }
}

class FakeTimeProvider : TimeProvider {
    override val currentTime: Instant = Instant.DISTANT_PAST
}