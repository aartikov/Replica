import me.aartikov.gradle.setupBinaryCompatibilityValidator
import me.aartikov.gradle.setupDokka
import me.aartikov.gradle.setupPublication
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.setup)
}

setupDokka()
setupPublication()
setupBinaryCompatibilityValidator()

kotlin {
    jvm {
        compilerOptions {
            optIn.add("kotlin.RequiresOptIn")
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    js(IR) {
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":replica-core"))
            api(libs.coroutines.core)
        }

        jvmTest.dependencies {
            implementation(libs.junit)
            implementation(libs.coroutines.test)
        }
    }
}
