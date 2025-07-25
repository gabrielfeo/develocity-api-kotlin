@file:Suppress("UnstableApiUsage")

package com.gabrielfeo

plugins {
    id("org.jetbrains.kotlin.jvm")
    `java-library`
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
        vendor = JvmVendorSpec.AZUL
    }
    consistentResolution {
        useRuntimeClasspathVersions()
    }
}

@Suppress("unused")
testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useKotlinTest()
        }
    }
}
