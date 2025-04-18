plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.dokka)
    alias(libs.plugins.binary.compatibility.validator)
}

apply(from = "${rootDir}/publish.gradle")

android {
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }

    namespace = "me.aartikov.replica.devtools.noop"
}

dependencies {
    coreLibraryDesugaring(libs.android.desugar)
    api(project(":replica-core"))
    testImplementation(libs.junit)
}