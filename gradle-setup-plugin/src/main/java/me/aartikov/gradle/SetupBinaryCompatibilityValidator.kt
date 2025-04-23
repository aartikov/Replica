package me.aartikov.gradle

import org.gradle.api.Project

fun Project.setupBinaryCompatibilityValidator() {
    plugins.apply(libs.plugins.binary.compatibility.validator.get().pluginId)
}
