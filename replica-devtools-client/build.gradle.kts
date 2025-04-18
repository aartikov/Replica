plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
}

kotlin {
    js(IR) {
        browser {
            distribution {
                directory = file("$projectDir/../replica-devtools/src/main/resources/replica-devtools/")
            }
        }
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.kotlin.datetime)
            }
        }

        val jsMain by getting {
            kotlin.srcDir("src/main/kotlin")
            resources.srcDir("src/main/resources")

            dependencies {
                implementation(project(":replica-devtools-dto"))
                implementation(compose.web.core)
                implementation(compose.runtime)
                implementation(libs.serialization.json)

                api(libs.coroutines.core)
                implementation(libs.bundles.ktor.client)
            }
        }
    }
}