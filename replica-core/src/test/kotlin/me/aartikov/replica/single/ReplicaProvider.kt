package me.aartikov.replica.single

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Instant
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.network.NetworkConnectivityProvider
import me.aartikov.replica.time.TimeProvider

class ReplicaProvider {

    val timeProvider = FakeTimeProvider()

    private val defaultReplicaSettings = ReplicaSettings.WithoutBehaviour

    private val defaultFetcher = Fetcher { TEST_DATA }

    private val defaultNetworkConnectivityProvider = FakeNetworkConnectivityProvider(
        MutableStateFlow(true)
    )

    companion object {
        const val TEST_DATA = "test"
    }

    fun replica(
        timeProvider: TimeProvider = this.timeProvider,
        fetcher: Fetcher<String> = defaultFetcher,
        replicaSettings: ReplicaSettings = defaultReplicaSettings,
        networkConnectivityProvider: NetworkConnectivityProvider = defaultNetworkConnectivityProvider
    ): PhysicalReplica<String> {
        val replicaClient = ReplicaClient(
            timeProvider = timeProvider,
            networkConnectivityProvider = networkConnectivityProvider
        )

        return replicaClient.createReplica(
            name = "test",
            settings = replicaSettings,
            fetcher = fetcher
        )
    }
}

class FakeTimeProvider : TimeProvider {
    override var currentTime: Instant = Instant.fromEpochMilliseconds(1000)
}

class FakeNetworkConnectivityProvider(
    override val connectedFlow: StateFlow<Boolean>
) : NetworkConnectivityProvider