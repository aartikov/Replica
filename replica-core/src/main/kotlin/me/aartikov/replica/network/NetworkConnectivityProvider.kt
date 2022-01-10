package me.aartikov.replica.network

import kotlinx.coroutines.flow.StateFlow

interface NetworkConnectivityProvider {
    val connected: StateFlow<Boolean>
}