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

testing {
    suites {
        named<JvmTestSuite>("test") {
            useKotlinTest()
        }
    }
}

val testTasks = tasks.named {
    it == "check" || it.contains("test", ignoreCase = true)
}

tasks.named { it.startsWith("publish") }.configureEach {
    mustRunAfter(testTasks)
}
