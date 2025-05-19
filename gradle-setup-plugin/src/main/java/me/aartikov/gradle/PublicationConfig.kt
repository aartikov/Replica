package me.aartikov.gradle

data class PublicationConfig(
    val group: String = "com.github.aartikov",
    val version: String = "1.5.1-alpha",
    val projectName: String = "Replica",
    val projectDescription: String = "Kotlin Multiplatform library for organizing of network communication in a declarative way",
    val projectUrl: String = "https://github.com/aartikov/Replica",
    val licenseName: String = "The MIT License",
    val licenseUrl: String = "https://github.com/aartikov/Replica/blob/master/LICENSE",
    val developerId: String = "aartikov",
    val developerName: String = "Artur Artikov",
    val developerEmail: String = "a.artikov@gmail.com",
    val scmUrl: String = "scm:git:git://github.com/aartikov/Replica.git",
    val repositoryUserName: String? = null,
    val repositoryPassword: String? = null,
    val signingKey: String? = null,
    val signingPassword: String? = null
) {
    val repositoryUrl: String
        get() = if (version.endsWith("SNAPSHOT")) SNAPSHOT_URL else RELEASE_URL

    companion object {
        private const val RELEASE_URL = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
        private const val SNAPSHOT_URL = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
    }
}
