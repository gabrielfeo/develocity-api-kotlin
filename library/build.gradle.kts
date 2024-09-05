@file:Suppress("UnstableApiUsage")

import java.net.URL

plugins {
    id("com.gabrielfeo.kotlin-jvm-library")
    id("com.gabrielfeo.develocity-api-code-generation")
    id("com.gabrielfeo.test-suites")
    `java-library`
    `maven-publish`
    signing
    alias(libs.plugins.kotlin.jupyter)
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
        implementation(libs.okio)
    }
    api(libs.moshi)
    implementation(libs.moshi.kotlin)
    api(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    api(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.retrofit.converter.scalars)
    api(libs.kotlin.coroutines)
    implementation(libs.slf4j.api)
    runtimeOnly(libs.slf4j.simple)
    compileOnly(libs.kotlin.jupyter.api)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.okio)
    testImplementation(libs.kotlin.coroutines.test)
    integrationTestImplementation(libs.kotlin.coroutines.test)
    integrationTestImplementation(libs.guava)
    integrationTestImplementation(libs.kotlin.jupyter.testkit)
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
