package com.gabrielfeo

plugins {
    id("org.jetbrains.kotlin.jvm")
    `java-test-fixtures`
}

testing {
    suites {
        register<JvmTestSuite>("integrationTest") {
            useKotlinTest()
        }
    }
}

kotlin {
    target {
        val main by compilations.getting
        val integrationTest by compilations.getting
        integrationTest.associateWith(main)
    }
}

tasks.named("check") {
    dependsOn("integrationTest")
}
