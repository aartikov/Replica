plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.binary.compatibility.validator) apply false
    alias(libs.plugins.jetbrains.compose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.dokka)
}

subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }

    plugins.withId("org.jetbrains.kotlin.multiplatform") {
        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
            compilerOptions {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }
}

dokka.pluginsConfiguration.html.footerMessage = "Copyright © 2025 Artur Artikov"

dependencies {
    dokka(project(":replica-core"))
    dokka(project(":replica-algebra"))
    dokka(project(":replica-android-network"))
    dokka(project(":replica-view-model"))
    dokka(project(":replica-decompose"))
    dokka(project(":replica-devtools"))
    dokka(project(":replica-devtools-noop"))
}