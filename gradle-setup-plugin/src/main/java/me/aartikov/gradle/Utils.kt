package me.aartikov.gradle

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.the

internal inline fun <reified T : Any> Project.hasExtension(): Boolean =
    try {
        extensions.findByType(T::class) != null
    } catch (_: NoClassDefFoundError) {
        false
    }

internal val Project.libs
    get() = the<LibrariesForLibs>()
