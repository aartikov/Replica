package me.aartikov.replica.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import me.aartikov.replica.common.ReplicaObserverHost

class TestObserverHost(active: Boolean) : ReplicaObserverHost {

    override val observerCoroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main.immediate)

    override val observerActive: MutableStateFlow<Boolean> = MutableStateFlow(active)

    var active by observerActive::value

    fun cancelCoroutineScope() {
        observerCoroutineScope.cancel()
    }
}