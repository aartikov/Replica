import me.aartikov.gradle.setupBinaryCompatibilityValidator
import me.aartikov.gradle.setupDokka
import me.aartikov.gradle.setupPublication
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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
            jvmTarget = JvmTarget.JVM_1_8
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
            api(libs.coroutines.core)
            api(libs.kotlin.datetime)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.coroutines.test)
        }

        jvmTest.dependencies {
            implementation(libs.junit)
        }
    }
}
