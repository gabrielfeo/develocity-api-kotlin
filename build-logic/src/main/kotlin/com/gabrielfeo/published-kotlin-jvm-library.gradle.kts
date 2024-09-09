plugins {
    id("com.gabrielfeo.kotlin-jvm-library")
    `java-library`
    `maven-publish`
    signing
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
}

publishing {
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
