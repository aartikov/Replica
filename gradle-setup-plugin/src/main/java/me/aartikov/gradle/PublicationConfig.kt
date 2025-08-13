package me.aartikov.gradle

data class PublicationConfig(
    val group: String = "com.github.aartikov",
    val version: String = "1.5.2-alpha",
    val projectName: String = "Replica",
    val projectDescription: String = "Kotlin Multiplatform library for organizing of network communication in a declarative way",
    val projectUrl: String = "https://github.com/aartikov/Replica",
    val licenseName: String = "The MIT License",
    val licenseUrl: String = "https://github.com/aartikov/Replica/blob/master/LICENSE",
    val developerId: String = "aartikov",
    val developerName: String = "Artur Artikov",
    val developerEmail: String = "a.artikov@gmail.com",
    val scmUrl: String = "scm:git:git://github.com/aartikov/Replica.git",
    val publishingUrl: String = "https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/",
    val repositoryUserName: String? = null,
    val repositoryPassword: String? = null,
    val signingKey: String? = null, // ascii armored gpg key
    val signingPassword: String? = null
)