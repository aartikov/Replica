package me.aartikov.replica.algebra.utils

import kotlinx.coroutines.flow.MutableStateFlow
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.network.NetworkConnectivityProvider
import me.aartikov.replica.time.TimeProvider

class ReplicaClientProvider {

    fun client(
        timeProvider: TimeProvider = FakeTimeProvider(),
        networkConnectivityProvider: NetworkConnectivityProvider = FakeNetworkConnectivityProvider(
            MutableStateFlow(true)
        )
    ): ReplicaClient {
        return ReplicaClient(
            timeProvider = timeProvider,
            networkConnectivityProvider = networkConnectivityProvider
        )
    }
}