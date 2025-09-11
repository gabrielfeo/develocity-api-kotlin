package com.gabrielfeo

import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import java.net.URI

plugins {
    id("com.gabrielfeo.kotlin-jvm-library")
    `java-library`
    `maven-publish`
    signing
    id("com.vanniktech.maven.publish.base")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
    id("org.jetbrains.dokka")
}

java {
    withSourcesJar()
    withJavadocJar()
}


configure<DokkaExtension> {
    val kotlinSourceRoot = file("src/main/kotlin")
    val repoUrlSuffix = "/blob/$version/${kotlinSourceRoot.relativeTo(rootDir)}"
    val repoUrl = providers.gradleProperty("repo.url")
        .map { URI("$it$repoUrlSuffix").toString() }
    dokkaSourceSets.configureEach {
        sourceRoots.from(kotlinSourceRoot)
        sourceLink {
            localDirectory.set(kotlinSourceRoot)
            remoteUrl(repoUrl)
            remoteLineSuffix = "#L"
        }
        jdkVersion = java.toolchain.languageVersion.map { it.asInt() }
        documentedVisibilities.add(VisibilityModifier.Public)
        suppressGeneratedFiles = false
        perPackageOption {
            matchingRegex = """.*\.internal.*"""
            suppress = true
        }
        listOf(
            "https://kotlinlang.org/api/kotlinx.coroutines",
            "https://square.github.io/okhttp/5.x/okhttp",
            "https://square.github.io/retrofit/2.x/retrofit",
            "https://square.github.io/moshi/1.x/moshi",
            "https://square.github.io/moshi/1.x/moshi-kotlin",
        ).forEach { url ->
            val name = url.trim('/').substringAfterLast('/')
            externalDocumentationLinks.register(name) {
                url(url)
                packageListUrl("$url/package-list")
            }
        }
    }
}

tasks.named<Jar>("javadocJar") {
    from(tasks.dokkaGenerate)
}

mavenPublishing {
    publishToMavenCentral()
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
