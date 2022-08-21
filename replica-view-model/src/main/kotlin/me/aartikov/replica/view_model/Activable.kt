package me.aartikov.replica.view_model

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Something with two states: active and inactive.
 */
interface Activable {
    val activeFlow: MutableStateFlow<Boolean>
}

/**
 * Creates an [Activable].
 */
fun activable() = object : Activable {
    override val activeFlow = MutableStateFlow(false)
}