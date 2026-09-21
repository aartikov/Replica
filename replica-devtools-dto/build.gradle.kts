import me.aartikov.gradle.setupBinaryCompatibilityValidator
import me.aartikov.gradle.setupPublication
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.setup)
}

setupPublication()
setupBinaryCompatibilityValidator()

kotlin {
    jvm()

    js(IR) {
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain {
            kotlin.srcDir("src/commonMain/kotlin")
            dependencies {
                implementation(libs.serialization.core)
                implementation(libs.coroutines.core)
                implementation(libs.kotlin.datetime)
            }
        }
    }
}
