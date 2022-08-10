package me.aartikov.replica.simple_sample.core.vm

import kotlinx.coroutines.flow.MutableStateFlow

interface Activable {
    val activeFlow: MutableStateFlow<Boolean>
}

fun activable() = object : Activable {
    override val activeFlow = MutableStateFlow(false)
}