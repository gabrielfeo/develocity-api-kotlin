@file:Suppress("UnstableApiUsage")

plugins {
    `kotlin-dsl`
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
        vendor = JvmVendorSpec.AZUL
    }
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

tasks.named("check") {
    dependsOn(tasks.withType<Test>())
}

dependencies {
    implementation(libs.kotlin.plugin)
    implementation(libs.kotlin.binary.compatibility.validator.plugin)
    implementation(libs.dokka.plugin)
    implementation(libs.openapi.generator.plugin)
    "functionalTestImplementation"(project)
}
