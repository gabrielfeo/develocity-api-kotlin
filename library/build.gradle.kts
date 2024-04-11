@file:Suppress("UnstableApiUsage")

import java.net.URL
import org.jetbrains.dokka.DokkaConfiguration.Visibility.PUBLIC
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    id("com.gabrielfeo.kotlin-jvm-library")
    id("com.gabrielfeo.develocity-api-code-generation")
    id("com.gabrielfeo.test-suites")
    `java-library`
    `maven-publish`
    signing
    kotlin("jupyter.api") version "0.12.0-181"
}

tasks.processJupyterApiResources {
    libraryProducers = listOf(
        "com.gabrielfeo.develocity.api.internal.jupyter.DevelocityApiJupyterIntegration",
    )
}

tasks.named<Test>("integrationTest") {
    environment("DEVELOCITY_API_LOG_LEVEL", "DEBUG")
}

java {
    consistentResolution {
        useRuntimeClasspathVersions()
    }
}

dependencies {
    constraints {
        implementation("com.squareup.okio:okio:3.9.0")
    }
    api("com.squareup.moshi:moshi:1.15.1")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    api("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    api("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.11.0")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.slf4j:slf4j-api:2.0.12")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.12")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("com.squareup.okio:okio:3.9.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    integrationTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    integrationTestImplementation("com.google.guava:guava:33.1.0-jre")
    integrationTestImplementation("org.jetbrains.kotlinx:kotlin-jupyter-test-kit:0.12.0-188")
}

val libraryPom = Action<MavenPom> {
    name.set("Develocity API Kotlin")
    description.set("A library to use the Develocity API in Kotlin")
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
            id.set("gabrielfeo")
            name.set("Gabriel Feo")
            email.set("gabriel@gabrielfeo.com")
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
            artifactId = "develocity-api-kotlin"
            from(components["java"])
            pom(libraryPom)
        }
        // For occasional maven local publishing
        register<MavenPublication>("unsignedDevelocityApiKotlin") {
            artifactId = "develocity-api-kotlin"
            from(components["java"])
            pom(libraryPom)
        }
        register<MavenPublication>("relocation") {
            artifactId = "gradle-enterprise-api-kotlin"
            pom {
                libraryPom(this)
                distributionManagement {
                    relocation {
                        groupId = project.group.toString()
                        artifactId = "develocity-api-kotlin"
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
