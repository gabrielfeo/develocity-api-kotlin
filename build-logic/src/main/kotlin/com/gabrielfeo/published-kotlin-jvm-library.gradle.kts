package com.gabrielfeo

import org.jetbrains.dokka.DokkaConfiguration.Visibility.PUBLIC
import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URL

plugins {
    id("com.gabrielfeo.kotlin-jvm-library")
    `java-library`
    `maven-publish`
    signing
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
    id("org.jetbrains.dokka")
}

java {
    withSourcesJar()
    withJavadocJar()
}

val kotlinSourceRoot = file("src/main/kotlin")
tasks.withType<DokkaTask>().configureEach {
    dokkaSourceSets.all {
        sourceRoot(kotlinSourceRoot)
        sourceLink {
            localDirectory = kotlinSourceRoot
            remoteUrl = providers.gradleProperty("repo.url")
                .map { URL("$it/blob/$version/${kotlinSourceRoot.relativeTo(rootDir)}") }
            remoteLineSuffix = "#L"
        }
        jdkVersion = java.toolchain.languageVersion.map { it.asInt() }
        suppressGeneratedFiles = false
        documentedVisibilities = setOf(PUBLIC)
        perPackageOption {
            matchingRegex = """.*\.internal.*"""
            suppress = true
        }
        externalDocumentationLink("https://kotlinlang.org/api/kotlinx.coroutines/")
        externalDocumentationLink("https://square.github.io/okhttp/5.x/okhttp/")
        externalDocumentationLink("https://square.github.io/retrofit/2.x/retrofit/")
        externalDocumentationLink("https://square.github.io/moshi/1.x/moshi/")
        externalDocumentationLink("https://square.github.io/moshi/1.x/moshi-kotlin/")
    }
}

tasks.named<Jar>("javadocJar") {
    from(tasks.dokkaHtml)
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
