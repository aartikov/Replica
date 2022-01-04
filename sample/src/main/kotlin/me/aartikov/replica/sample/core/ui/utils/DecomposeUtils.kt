package me.aartikov.replica.sample.core.ui.utils

import com.arkivanov.decompose.Child
import com.arkivanov.decompose.router.RouterState

fun <T : Any> createFakeRouterState(instance: T): RouterState<*, T> {
    return RouterState(
        activeChild = Child.Created(
            configuration = "<fake>",
            instance = instance
        )
    )
}