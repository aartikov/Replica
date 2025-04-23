import me.aartikov.gradle.setupBinaryCompatibilityValidator
import me.aartikov.gradle.setupDokka
import me.aartikov.gradle.setupPublication

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.setup)
}

setupDokka()
setupPublication()
setupBinaryCompatibilityValidator()

android {
    namespace = "me.aartikov.replica.devtools.noop"

    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    api(project(":replica-core"))
    testImplementation(libs.junit)
}