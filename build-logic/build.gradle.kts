@file:Suppress("UnstableApiUsage")

plugins {
    `kotlin-dsl`
}

testing {
    suites {
        register<JvmTestSuite>("functionalTest") {
            useJUnitJupiter()
        }
    }
}

gradlePlugin {
    testSourceSets(sourceSets["functionalTest"])
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.23")
    implementation("org.openapitools:openapi-generator-gradle-plugin:7.3.0")
    "functionalTestImplementation"(project)
}
