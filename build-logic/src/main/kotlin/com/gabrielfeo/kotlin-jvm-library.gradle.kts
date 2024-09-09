package com.gabrielfeo

import org.jetbrains.dokka.DokkaConfiguration.Visibility.PUBLIC
import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URL

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
    `java-library`
}

val repoUrl: Provider<String> = providers.gradleProperty("repo.url")

java {
    withSourcesJar()
    withJavadocJar()
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
        vendor = JvmVendorSpec.AZUL
    }
    consistentResolution {
        useRuntimeClasspathVersions()
    }
}

val kotlinSourceRoot = file("src/main/kotlin")
tasks.withType<DokkaTask>().configureEach {
    dokkaSourceSets.all {
        sourceRoot(kotlinSourceRoot)
        sourceLink {
            localDirectory = kotlinSourceRoot
            remoteUrl = repoUrl.map { URL("$it/blob/$version/${kotlinSourceRoot.relativeTo(rootDir)}") }
            remoteLineSuffix = "#L"
        }
        jdkVersion = 11
        suppressGeneratedFiles = false
        documentedVisibilities = setOf(PUBLIC)
        perPackageOption {
            matchingRegex = """.*\.internal.*"""
            suppress = true
        }
        externalDocumentationLink("https://kotlinlang.org/api/kotlinx.coroutines/")
        externalDocumentationLink("https://square.github.io/okhttp/4.x/okhttp/")
        externalDocumentationLink("https://square.github.io/retrofit/2.x/retrofit/")
        externalDocumentationLink("https://square.github.io/moshi/1.x/moshi/")
        externalDocumentationLink("https://square.github.io/moshi/1.x/moshi-kotlin/")
    }
}

tasks.named<Jar>("javadocJar") {
    from(tasks.dokkaHtml)
}
