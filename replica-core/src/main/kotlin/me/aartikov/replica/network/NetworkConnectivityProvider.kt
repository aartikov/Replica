package me.aartikov.replica.network

import kotlinx.coroutines.flow.StateFlow

interface NetworkConnectivityProvider {
    val connectedFlow: StateFlow<Boolean>
}

val NetworkConnectivityProvider.connected: Boolean get() = connectedFlow.value