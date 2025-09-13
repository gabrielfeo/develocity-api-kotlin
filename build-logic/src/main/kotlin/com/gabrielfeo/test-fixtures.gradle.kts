package com.gabrielfeo

plugins {
    id("org.jetbrains.kotlin.jvm")
    `java-test-fixtures`
}

kotlin {
    target {
        val main by compilations.getting
        val testFixtures by compilations.getting
        testFixtures.associateWith(main)
        compilations.named { it.endsWith("test", ignoreCase = true) }.configureEach {
            associateWith(testFixtures)
        }
    }
}

components.named<AdhocComponentWithVariants>("java") {
    withVariantsFromConfiguration(configurations["testFixturesApiElements"]) { skip() }
    withVariantsFromConfiguration(configurations["testFixturesRuntimeElements"]) { skip() }
}
