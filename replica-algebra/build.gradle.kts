plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.dokka)
    alias(libs.plugins.binary.compatibility.validator)
}

apply(from = "${rootDir}/publish.gradle")

kotlin {
    jvm {
        compilations.configureEach {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

    ios()

    js(IR) {
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":replica-core"))
                api(libs.coroutines.core)
            }
        }
        
        val jvmTest by getting {
            dependencies {
                implementation(libs.junit)
                implementation(libs.coroutines.test)
            }
        }
    }
}