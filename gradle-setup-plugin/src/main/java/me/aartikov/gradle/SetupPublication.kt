package me.aartikov.gradle

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.io.File
import java.util.Properties

fun Project.setupPublication() {
    val config = loadPublicationConfig()

    when {
        hasExtension<KotlinMultiplatformExtension>() -> setupMultiplatformPublication(config)
        hasExtension<LibraryExtension>() -> setupAndroidLibraryPublication(config)
        else -> error("Unsupported project type for publication")
    }
}

private fun Project.loadPublicationConfig(): PublicationConfig {
    val properties = Properties().apply {
        rootProject.file("local.properties")
            .takeIf(File::exists)
            ?.reader()
            ?.use(::load)
    }

    fun get(name: String, env: String) = properties.getProperty(name) ?: System.getenv(env)

    return PublicationConfig(
        repositoryUserName = get("ossrhUsername", "OSSRH_USERNAME"),
        repositoryPassword = get("ossrhPassword", "OSSRH_PASSWORD"),
        signingKey = get(
            "signing.secretKeyRingFile",
            "SIGNING_SECRET_KEY_RING_FILE"
        )?.let { File(it).readText() },
        signingPassword = get("signing.password", "SIGNING_PASSWORD")
    )
}

private fun Project.setupMultiplatformPublication(config: PublicationConfig) {
    applyMavenPublishPlugin()

    group = config.group
    version = config.version

    extensions.configure<PublishingExtension> {
        publications.withType<MavenPublication>().configureEach {
            artifact(project.ensureJavadocJarTask())
            setupPom(config)
        }

        setupRepository(config)
    }

    setupSigning(config)
}

private fun Project.setupAndroidLibraryPublication(config: PublicationConfig) {
    applyMavenPublishPlugin()

    extensions.configure<LibraryExtension> {
        publishing {
            singleVariant("release") {
                withSourcesJar()
            }
        }
    }

    extensions.configure<PublishingExtension> {
        publications {
            register<MavenPublication>("release") {
                groupId = config.group
                version = config.version
                artifactId = project.name

                artifact(project.ensureJavadocJarTask())

                setupPom(config)

                afterEvaluate {
                    from(components["release"])
                }
            }
        }

        setupRepository(config)
    }

    setupSigning(config)
}

private fun MavenPublication.setupPom(config: PublicationConfig) {
    pom {
        name.set(config.projectName)
        description.set(config.projectDescription)
        url.set(config.projectUrl)

        licenses {
            license {
                name.set(config.licenseName)
                url.set(config.licenseUrl)
            }
        }

        developers {
            developer {
                id.set(config.developerId)
                name.set(config.developerName)
                email.set(config.developerEmail)
            }
        }

        scm {
            url.set(config.projectUrl)
            connection.set(config.scmUrl)
            developerConnection.set(config.scmUrl)
        }
    }
}

private fun PublishingExtension.setupRepository(config: PublicationConfig) {
    repositories {
        maven {
            name = "sonatype"

            setUrl(config.repositoryUrl)

            credentials {
                username = config.repositoryUserName
                password = config.repositoryPassword
            }
        }
    }
}

private fun Project.applyMavenPublishPlugin() {
    plugins.apply("maven-publish")
}

private fun Project.ensureJavadocJarTask(): Task = tasks.run {
    findByName("javadocJar") ?: create<Jar>("javadocJar").apply {
        archiveClassifier.set("javadoc")
        findByName("dokkaGeneratePublicationHtml")?.let { dokkaTask ->
            dependsOn(dokkaTask)
            from(dokkaTask)
        }
    }
}

private fun Project.setupSigning(config: PublicationConfig) {
    if (config.signingKey != null && config.signingPassword != null) {
        plugins.apply("signing")

        extensions.configure<SigningExtension> {
            useInMemoryPgpKeys(config.signingKey, config.signingPassword)
            sign(extensions.getByType<PublishingExtension>().publications)
        }

        // Workaround for https://github.com/gradle/gradle/issues/26091
        tasks.withType<AbstractPublishToMaven>().configureEach {
            mustRunAfter(tasks.withType<Sign>())
        }
    }
}
