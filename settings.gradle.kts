pluginManagement {
    includeBuild("gradle-setup-plugin")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        }
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        }
    }
}

rootProject.name = "Replica"

include(":replica-core")
include(":replica-algebra")
include(":replica-android-network")
include(":replica-view-model")
include(":replica-decompose")
include(":replica-devtools")
include(":replica-devtools-noop")
include(":replica-devtools-client")
include(":replica-devtools-dto")
include(":advanced-sample")
include(":simple-sample")