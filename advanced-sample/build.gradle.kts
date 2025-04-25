plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "me.aartikov.replica.advanced_sample"

    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "me.aartikov.replica.advanced_sample"
        versionName = "1.0"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources.excludes += setOf(
            "/META-INF/{AL2.0,LGPL2.1}",
            "/META-INF/INDEX.LIST",
            "/META-INF/io.netty.versions.properties"
        )
    }
}

dependencies {
    coreLibraryDesugaring(libs.android.desugar)

    implementation(project(":replica-core"))
    implementation(project(":replica-algebra"))
    implementation(project(":replica-android-network"))
    implementation(project(":replica-decompose"))
    debugImplementation(project(":replica-devtools"))

    // Kotlin
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // UI
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.preview)
    debugImplementation(libs.compose.tooling)
    implementation(libs.activity.compose)
    implementation(libs.appcompat)
    implementation(libs.coilCompose)
    implementation(libs.splashscreen)

    // Architecture
    implementation(libs.sesame.localizedString)
    implementation(libs.bundles.decompose)

    // Network
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.serialization)
    implementation(libs.okhttp.logging)

    // Serialization
    implementation(libs.serialization.json)

    // DI
    implementation(libs.koin.android)

    // Debugging
    implementation(libs.timber)
    debugImplementation(libs.chucker)
    debugImplementation(libs.bundles.hyperion)
}