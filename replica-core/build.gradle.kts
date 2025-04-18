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
                api(libs.coroutines.core)
                api(libs.kotlin.datetime)
            }
        }

        val jvmMain by getting {}

/*        val iosMain by getting {}
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }*/

        val jsMain by getting {}

        val jvmTest by getting {
            dependencies {
                implementation(libs.junit)
                implementation(libs.coroutines.test)
            }
        }
    }
}