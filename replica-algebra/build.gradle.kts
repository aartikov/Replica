plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.dokka)
    alias(libs.plugins.binary.compatibility.validator)
}

apply(from = "${rootDir}/publish.gradle")

kotlin {
    jvm {
        compilerOptions {
            optIn.add("kotlin.RequiresOptIn")
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    js(IR) {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":replica-core"))
            api(libs.coroutines.core)
        }

        jvmTest.dependencies {
            implementation(libs.junit)
            implementation(libs.coroutines.test)
        }
    }
}