import me.aartikov.gradle.setupBinaryCompatibilityValidator
import me.aartikov.gradle.setupDokka
import me.aartikov.gradle.setupPublication
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

    sourceSets {
        commonMain.dependencies {
            api(libs.coroutines.core)
            api(libs.kotlin.datetime)
        }

        jvmTest.dependencies {
            implementation(libs.junit)
            implementation(libs.coroutines.test)
        }
    }
}