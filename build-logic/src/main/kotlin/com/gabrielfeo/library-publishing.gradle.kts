package com.gabrielfeo

plugins {
  `java-library`
  `maven-publish`
  signing
}

val libraryPom = Action<MavenPom> {
    name.set(providers.gradleProperty("library.pom.name"))
    description.set(providers.gradleProperty("library.pom.description"))
    val repoUrl = providers.gradleProperty("repo.url")
    url.set(repoUrl)
    licenses {
        license {
            name.set("MIT")
            url.set("https://spdx.org/licenses/MIT.html")
            distribution.set("repo")
        }
    }
    developers {
        developer {
            id.set(providers.gradleProperty("library.pom.developer.id"))
            name.set(providers.gradleProperty("library.pom.developer.name"))
            email.set(providers.gradleProperty("library.pom.developer.email"))
        }
    }
    scm {
        val basicUrl = repoUrl.map { it.substringAfter("://") }
        connection.set(basicUrl.map { "scm:git:git://$it.git" })
        developerConnection.set(basicUrl.map { "scm:git:ssh://$it.git" })
        url.set(basicUrl.map { "https://$it/" })
    }
}

publishing {
    publications {
        register<MavenPublication>("develocityApiKotlin") {
            artifactId = properties["library.artifact"]
            from(components["java"])
            pom(libraryPom)
        }
        // For occasional maven local publishing
        register<MavenPublication>("unsignedDevelocityApiKotlin") {
            artifactId = providers.gradleProperty("library.artifact")
            from(components["java"])
            pom(libraryPom)
        }
        register<MavenPublication>("relocation") {
            artifactId = providers.gradleProperty("library.artifact.old")
            pom {
                libraryPom(this)
                distributionManagement {
                    relocation {
                        groupId = providers.gradleProperty("library.group")
                        artifactId = providers.gradleProperty("library.artifact")
                        message = "artifactId has been changed. Part of the rename to Develocity."
                    }
                }
            }
        }
    }
    repositories {
        maven {
            name = "mavenCentral"
            val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            val isSnapshot = version.toString().endsWith("SNAPSHOT")
            url = if (isSnapshot) snapshotsRepoUrl else releasesRepoUrl
            authentication {
                register<BasicAuthentication>("basic")
            }
            credentials {
                username = project.properties["maven.central.username"] as String?
                password = project.properties["maven.central.password"] as String?
            }
        }
    }
}

fun isCI() = System.getenv("CI").toBoolean()

signing {
    val signedPublications = publishing.publications.matching {
        !it.name.contains("unsigned", ignoreCase = true)
    }
    sign(signedPublications)
    if (isCI()) {
        useInMemoryPgpKeys(
            project.properties["signing.secretKey"] as String?,
            project.properties["signing.password"] as String?,
        )
    }
}
