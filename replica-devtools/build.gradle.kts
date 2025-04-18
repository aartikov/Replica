plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.dokka)
    alias(libs.plugins.binary.compatibility.validator)
}

apply(from = "${rootDir}/publish.gradle")

android {
    namespace = "me.aartikov.replica.devtools"

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
}

afterEvaluate {
    tasks.findByPath(":replica-devtools:processDebugJavaRes")
        ?.dependsOn(tasks.findByPath(":replica-devtools-client:jsBrowserDistribution"))

    tasks.findByPath(":replica-devtools:processReleaseJavaRes")
        ?.dependsOn(tasks.findByPath(":replica-devtools-client:jsBrowserDistribution"))
}

dependencies {
    coreLibraryDesugaring(libs.android.desugar)
    api(project(":replica-core"))
    implementation(project(":replica-devtools-dto"))
    api(libs.coroutines.core)
    implementation(libs.serialization.core)
    implementation(libs.serialization.json)
    implementation(libs.kotlin.datetime)
    implementation(libs.bundles.ktor.server)

    testImplementation(libs.junit)
}