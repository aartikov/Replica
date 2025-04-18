plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "me.aartikov.replica.simple_sample"
        versionName = "1.0"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        buildConfigField("String", "BACKEND_URL", "\"https://pokeapi.co/\"")
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    packaging {
        resources.excludes.add("META-INF/*")
    }

    namespace = "me.aartikov.replica.simple_sample"
}

dependencies {
    coreLibraryDesugaring(libs.android.desugar)

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
    implementation(libs.fragment)
    implementation(libs.recyclerView)
    implementation(libs.material)
    implementation(libs.coil)
    implementation(libs.swipeRefresh)
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

    // Logging
    implementation(libs.timber)
}