package me.aartikov.replica.single.utils

import kotlinx.coroutines.flow.MutableStateFlow
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.network.NetworkConnectivityProvider
import me.aartikov.replica.single.Fetcher
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.time.TimeProvider
import me.aartikov.replica.utils.FakeNetworkConnectivityProvider
import me.aartikov.replica.utils.FakeTimeProvider

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