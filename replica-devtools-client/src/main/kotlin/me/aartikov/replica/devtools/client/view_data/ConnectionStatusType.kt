package me.aartikov.replica.devtools.client.view_data

import me.aartikov.replica.devtools.client.ConnectionStatus

enum class ConnectionStatusType(val text: String) {
    Failed("Connection failed"), Connected("Connected"), Connecting("Connecting")
}

fun ConnectionStatus.toViewData(): ConnectionStatusType {
    return when (this) {
        is ConnectionStatus.Connected -> ConnectionStatusType.Connected
        is ConnectionStatus.Failed -> ConnectionStatusType.Failed
        is ConnectionStatus.Attempt -> ConnectionStatusType.Connecting
    }
}