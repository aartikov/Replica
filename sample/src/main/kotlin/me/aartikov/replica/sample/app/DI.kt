package me.aartikov.replica.sample.app

import me.aartikov.replica.sample.core.coreModule
import me.aartikov.replica.sample.features.profile.profileModule

val allModules = listOf(
    coreModule,
    profileModule
)