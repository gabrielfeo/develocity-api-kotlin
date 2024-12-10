@file:Suppress("UnstableApiUsage")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.9.0")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
