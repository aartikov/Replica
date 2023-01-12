package me.aartikov.replica.algebra.utils

import kotlinx.coroutines.flow.StateFlow
import me.aartikov.replica.network.NetworkConnectivityProvider

class FakeNetworkConnectivityProvider(
    override val connectedFlow: StateFlow<Boolean>
) : NetworkConnectivityProvider