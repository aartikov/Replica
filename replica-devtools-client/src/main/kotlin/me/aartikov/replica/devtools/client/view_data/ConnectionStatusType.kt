package me.aartikov.replica.devtools.client.view_data

import me.aartikov.replica.devtools.client.ConnectionStatus

enum class ConnectionStatusType(val text: String) {
    Error("Connection failed"), Success("Connected"), Loading("Loading");
}

fun ConnectionStatus.toViewData(): ConnectionStatusType {
    return when (this) {
        is ConnectionStatus.Connected -> ConnectionStatusType.Success
        is ConnectionStatus.Failed -> ConnectionStatusType.Error
        is ConnectionStatus.Attempt -> ConnectionStatusType.Loading
    }
}