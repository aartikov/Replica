plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    js(IR) {
        browser {
            distribution {
                outputDirectory =
                    file("$projectDir/../replica-devtools/src/main/resources/replica-devtools/")
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlin.datetime)
        }

        jsMain {
            kotlin.srcDir("src/main/kotlin")
            resources.srcDir("src/main/resources")

            dependencies {
                implementation(project(":replica-devtools-dto"))
                implementation(compose.html.core)
                implementation(compose.runtime)
                implementation(libs.serialization.json)

                api(libs.coroutines.core)
                implementation(libs.bundles.ktor.client)
            }
        }
    }
}