import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "me.aartikov.replica.simple_sample"

    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "me.aartikov.replica.simple_sample"
        versionName = "1.0"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        buildConfigField("String", "BACKEND_URL", "\"https://pokeapi.co/\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    packaging {
        resources.excludes += setOf(
            "/META-INF/{AL2.0,LGPL2.1}",
            "/META-INF/INDEX.LIST",
            "/META-INF/io.netty.versions.properties"
        )
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
        freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
    }
}

dependencies {
    implementation(project(":replica-core"))
    implementation(project(":replica-algebra"))
    implementation(project(":replica-android-network"))
    implementation(project(":replica-view-model"))
    debugImplementation(project(":replica-devtools"))
    releaseImplementation(project(":replica-devtools-noop"))

    // Kotlin
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // UI
    implementation(libs.appcompat)

    // UI
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.preview)
    implementation(libs.compose.navgigaton)
    debugImplementation(libs.compose.tooling)
    implementation(libs.activity.compose)
    implementation(libs.coil.compose)
    implementation(libs.splashscreen)

    // Architecture
    implementation(libs.viewModel)
    implementation(libs.sesame.localizedString)

    // Network
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.serialization)
    implementation(libs.okhttp.logging)

    // Serialization
    implementation(libs.serialization.json)

    // DI
    implementation(libs.koin.android)
    implementation(libs.koin.android.compose)

    // Logging
    implementation(libs.timber)
}
