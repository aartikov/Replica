package me.aartikov.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.engine.plugins.DokkaHtmlPluginParameters

fun Project.setupDokka() {
    plugins.apply(libs.plugins.dokka.get().pluginId)

    extensions.configure<DokkaExtension> {
        pluginsConfiguration.named("html", DokkaHtmlPluginParameters::class.java) {
            footerMessage.set("Copyright © 2025 Artur Artikov")
        }
    }
}
