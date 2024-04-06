package com.gabrielfeo

plugins {
    id("org.jetbrains.kotlin.jvm")
    `java-test-fixtures`
}

testing {
    suites {
        // 'test' is registered by default
        register<JvmTestSuite>("integrationTest")
        withType<JvmTestSuite>().configureEach {
            useKotlinTest()
        }
    }
}

tasks.named("check") {
    dependsOn("integrationTest")
}

kotlin {
    target {
        val main by compilations.getting
        val integrationTest by compilations.getting
        val test by compilations.getting
        val testFixtures by compilations.getting
        test.associateWith(main)
        test.associateWith(testFixtures)
        integrationTest.associateWith(main)
        integrationTest.associateWith(testFixtures)
        testFixtures.associateWith(main)
    }
}

// TODO Unapply test-fixtures and delete the source set, since we're not publishing it?
components.named<AdhocComponentWithVariants>("java") {
    withVariantsFromConfiguration(configurations["testFixturesApiElements"]) { skip() }
    withVariantsFromConfiguration(configurations["testFixturesRuntimeElements"]) { skip() }
}
