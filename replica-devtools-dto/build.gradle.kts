plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.binary.compatibility.validator)
}

apply(from = "${rootDir}/publish.gradle")

kotlin {
    jvm()
    js(IR) {
        browser()
    }

    sourceSets {
        commonMain {
            kotlin.srcDir("src/commonMain/kotlin")
            dependencies {
                implementation(libs.serialization.core)
                implementation(libs.coroutines.core)
                implementation(libs.kotlin.datetime)
            }
        }
    }
}