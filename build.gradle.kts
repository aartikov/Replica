buildscript {
    extra.apply {
        set(
            "androidLibraryConfig", mapOf(
                "minSdkVersion" to 23,
                "compileSdkVersion" to 34
            )
        )
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.binary.compatibility.validator) apply false
    alias(libs.plugins.jetbrains.compose) apply false

    alias(libs.plugins.dokka) apply true
}

tasks.named<org.jetbrains.dokka.gradle.DokkaMultiModuleTask>("dokkaHtmlMultiModule") {
    pluginsMapConfiguration.set(
        mapOf("org.jetbrains.dokka.base.DokkaBase" to """{ "footerMessage": "Copyright © 2023 Artur Artikov" }""")
    )
}