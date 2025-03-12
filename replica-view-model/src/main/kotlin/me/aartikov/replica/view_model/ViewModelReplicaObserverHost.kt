package me.aartikov.replica.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import me.aartikov.replica.common.ReplicaObserverHost

fun <VM> VM.replicaObserverHost(): ReplicaObserverHost where VM : ViewModel, VM : Activable {
    return ReplicaObserverHost(viewModelScope, activeFlow)
}