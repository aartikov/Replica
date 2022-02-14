package me.aartikov.replica.algebra.utils

import kotlinx.coroutines.flow.MutableStateFlow
import me.aartikov.replica.network.NetworkConnectivityProvider
import me.aartikov.replica.single.Fetcher
import me.aartikov.replica.single.PhysicalReplica
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.single.Storage
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

    private val clientProvider = ReplicaClientProvider()

    fun replica(
        timeProvider: TimeProvider = this.timeProvider,
        fetcher: Fetcher<String> = defaultFetcher,
        replicaSettings: ReplicaSettings = defaultReplicaSettings,
        networkConnectivityProvider: NetworkConnectivityProvider = defaultNetworkConnectivityProvider,
        storage: Storage<String>? = null
    ): PhysicalReplica<String> {
        val replicaClient = clientProvider.client(
            timeProvider = timeProvider,
            networkConnectivityProvider = networkConnectivityProvider
        )

        return replicaClient.createReplica(
            name = "test",
            settings = replicaSettings,
            fetcher = fetcher,
            storage = storage
        )
    }
}